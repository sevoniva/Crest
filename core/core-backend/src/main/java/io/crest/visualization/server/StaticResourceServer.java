package io.crest.visualization.server;

import io.crest.api.visualization.StaticResourceApi;
import io.crest.api.visualization.request.StaticResourceRequest;
import io.crest.exception.DEException;
import io.crest.utils.FileUtils;
import io.crest.utils.JsonUtil;
import io.crest.utils.LogUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RestController
@RequestMapping("/staticResource")
@SuppressWarnings("unchecked")
public class StaticResourceServer implements StaticResourceApi {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".gif", ".svg", ".png", ".jpeg", ".jpg"
    );

    private static final Set<String> ALLOWED_SVG_MIME_TYPES = Set.of(
            "image/svg+xml",
            "image/svg-xml",
            "application/svg+xml"
    );

    @Value("${crest.path.static-resource:/opt/crest/data/static-resource/}")
    private String staticDir;

    @Override
    public void upload(String fileId, MultipartFile file) {
        // check if the path is valid (not outside staticDir)
        Assert.notNull(file, "Multipart file must not be null");
        try {
            String originName = file.getOriginalFilename();
            validateImageFilename(originName);
            String newFileName = fileId + originName.substring(originName.lastIndexOf("."), originName.length());
            validateImageFilename(newFileName);
            byte[] fileBytes = file.getBytes();
            validateImageContent(newFileName, fileBytes, file.getContentType(), true);
            writeFileIfAbsent(newFileName, fileBytes);
        } catch (DEException e) {
            throw e;
        } catch (IOException e) {
            LogUtil.error("文件上传失败", e);
            DEException.throwException("文件上传失败");
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String mimeType = file.getContentType();
        if (StringUtils.isEmpty(mimeType)) {
            return false;
        }
        if (!hasValidImageExtension(file.getOriginalFilename())) {
            return false;
        }
        // 判断是否为图片或SVG
        return (isImageOther(file)) || isValidSVG(file);
    }

    private boolean hasValidImageExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return false;
        }
        String lowerFilename = filename.toLowerCase(Locale.ROOT);
        for (String ext : ALLOWED_IMAGE_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }

    private boolean isImageOther(MultipartFile file) {
        BufferedImage image = null;
        try (InputStream input = file.getInputStream()) {
            image = ImageIO.read(input);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return false;
        }
        return true;
    }

    public void saveFilesToServe(String staticResource) {
        if (StringUtils.isNotEmpty(staticResource)) {
            Map<String, String> resource = JsonUtil.parse(staticResource, Map.class);
            if (resource == null || resource.isEmpty()) {
                return;
            }
            for (Map.Entry<String, String> entry : resource.entrySet()) {
                String path = entry.getKey();
                String fileName = extractFileName(path);
                saveSingleFileToServe(fileName, entry.getValue());
            }
        }
    }

    public void saveSingleFileToServe(String fileName, String content) {
        try {
            validateImageFilename(fileName);
            if (StringUtils.isEmpty(content)) {
                DEException.throwException("静态资源内容不能为空");
            }
            byte[] fileBytes = decodeBase64Content(content);
            validateImageContent(fileName, fileBytes, null, false);
            writeFileIfAbsent(fileName, fileBytes);
        } catch (DEException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("template static resource save error", e);
            DEException.throwException(e);
        }
    }

    @Override
    public Map<String, String> findResourceAsBase64(StaticResourceRequest resourceRequest) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resourceRequest.getResourcePathList())) {
            for (String path : resourceRequest.getResourcePathList()) {
                String value = readResourceAsBase64(extractFileName(path));
                result.put(path, value);
            }
        }
        return result;
    }

    private void validateImageFilename(String filename) {
        FileUtils.validateUploadFilename(filename);
        if (!hasValidImageExtension(filename)) {
            DEException.throwException("静态资源必须是图片");
        }
    }

    private String extractFileName(String path) {
        return StringUtils.substringAfterLast(path.replace("\\", "/"), "/");
    }

    private String readResourceAsBase64(String fileName) {
        if (StringUtils.isBlank(fileName) || !hasValidImageExtension(fileName)) {
            return null;
        }
        try {
            FileUtils.validateUploadFilename(fileName);
            Path basePath = Paths.get(staticDir).normalize();
            Path resourcePath = basePath.resolve(fileName).normalize();
            if (!resourcePath.startsWith(basePath) || !Files.isRegularFile(resourcePath)) {
                return null;
            }
            return Base64.getEncoder().encodeToString(Files.readAllBytes(resourcePath));
        } catch (RuntimeException e) {
            return null;
        } catch (IOException e) {
            LogUtil.debug(StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
            return null;
        }
    }

    private byte[] decodeBase64Content(String content) {
        try {
            return Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            DEException.throwException("静态资源Base64内容无效");
            return new byte[0];
        }
    }

    private void writeFileIfAbsent(String fileName, byte[] content) throws IOException {
        Path basePath = Paths.get(staticDir.toString());
        FileUtils.createIfAbsent(basePath);
        Path uploadPath = basePath.resolve(fileName).normalize();
        if (!uploadPath.startsWith(basePath.normalize())) {
            DEException.throwException("静态资源文件名非法");
        }
        if (Files.exists(uploadPath)) {
            LogUtil.info("file exists");
            return;
        }
        Files.write(uploadPath, content, StandardOpenOption.CREATE_NEW);
    }

    private void validateImageContent(String fileName, byte[] content, String mimeType, boolean checkSvgMimeType) {
        if (content == null || content.length == 0) {
            DEException.throwException("静态资源内容不能为空");
        }
        if (Strings.CI.endsWith(fileName, ".svg")) {
            validateSvgContent(content, mimeType, checkSvgMimeType);
            return;
        }
        if (!isImageOther(content)) {
            DEException.throwException("静态资源必须是图片");
        }
    }

    private boolean isImageOther(byte[] content) {
        try (InputStream input = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(input);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
    }

    private void validateSvgContent(byte[] content, String mimeType, boolean checkSvgMimeType) {
        if (checkSvgMimeType && !isValidSvgMimeType(mimeType)) {
            DEException.throwException("无效的SVG文件MIME类型");
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            if (!"svg".equals(doc.getDocumentElement().getNodeName())) {
                DEException.throwException("根元素必须是svg");
            }
            if (containsDangerousContent(doc)) {
                DEException.throwException("SVG包含不允许的脚本或事件处理器");
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("DOCTYPE")) {
                DEException.throwException("svg 内容禁止使用 DOCTYPE");
            } else {
                DEException.throwException("SVG解析失败: " + e.getMessage());
            }
        }
    }

    private static boolean isValidSVG(MultipartFile file){
        if (file == null || file.isEmpty()) {
            return false;
        }

        // MIME类型预检查
        if (!isValidSvgMimeType(file)) {
            DEException.throwException("无效的SVG文件MIME类型");
            return false;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try (InputStream inputStream = file.getInputStream()) {
            // 禁用外部实体解析以防止XXE攻击
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setNamespaceAware(true);

            // 启用安全解析设置
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);

            // 检查根元素是否是<svg>
            if (!"svg".equals(doc.getDocumentElement().getNodeName())) {
                DEException.throwException("根元素必须是svg");
                return false;
            }

            // 安全检查：如果发现任何危险内容，直接返回false
            if (containsDangerousContent(doc)) {
                DEException.throwException("SVG包含不允许的脚本或事件处理器");
                return false;
            }

            return true;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            // 如果出现任何解析错误，说明该文件不是合法的SVG
            if(e.getMessage() != null && e.getMessage().contains("DOCTYPE")){
                DEException.throwException("svg 内容禁止使用 DOCTYPE");
            } else {
                DEException.throwException("SVG解析失败: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * MIME类型检查
     */
    private static boolean isValidSvgMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        return isValidSvgMimeType(contentType);
    }

    private static boolean isValidSvgMimeType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return ALLOWED_SVG_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }

    /**
     * 检查SVG是否包含危险内容
     * @return true 包含危险内容，false 安全
     */
    private static boolean containsDangerousContent(Document doc) {
        // 危险的事件处理器属性
        Set<String> dangerousAttributes = new HashSet<>(Arrays.asList(
                "onload", "onerror", "onclick", "ondblclick", "onmousedown", "onmouseup",
                "onmouseover", "onmousemove", "onmouseout", "onfocus", "onblur",
                "onkeydown", "onkeypress", "onkeyup", "onsubmit", "onreset",
                "onchange", "onselect", "onabort", "onunload", "onresize",
                "onscroll", "oninput", "onactivate", "onbeforeactivate", "onbeforedeactivate",
                "ondeactivate", "onbegin", "onend", "onrepeat", "onloadstart",
                "onprogress", "onloadend", "oncanplay", "oncanplaythrough", "onwaiting",
                "onseeking", "onseeked", "ontimeupdate", "onplaying", "onpause",
                "onratechange", "ondurationchange", "onvolumechange"
        ));

        // 危险标签
        Set<String> dangerousTags = new HashSet<>(Arrays.asList(
                "script", "style", "object", "embed", "applet", "iframe",
                "frame", "frameset", "link", "meta", "base", "form"
        ));

        // 遍历所有元素
        NodeList elements = doc.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String tagName = element.getTagName().toLowerCase();

            // 检查危险标签
            if (dangerousTags.contains(tagName)) {
                return true; // 发现危险标签，返回true表示包含危险内容
            }

            // 检查属性
            NamedNodeMap attributes = element.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Attr attr = (Attr) attributes.item(j);
                String attrName = attr.getName().toLowerCase();
                String attrValue = attr.getValue().toLowerCase();

                // 检查事件处理器属性
                if (dangerousAttributes.contains(attrName)) {
                    return true;
                }

                // 检查属性名是否以"on"开头（事件处理器）
                if (attrName.startsWith("on") && attrName.length() > 2) {
                    return true;
                }

                // 检查属性值是否包含JavaScript相关代码
                if (containsJavaScript(attrValue)) {
                    return true;
                }
            }
        }

        // 检查文本节点
        if (containsScriptInText(doc)) {
            return true;
        }

        return false;
    }

    /**
     * 检查字符串是否包含JavaScript代码
     */
    private static boolean containsJavaScript(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        String lowerValue = value.toLowerCase();

        // JavaScript相关模式
        String[] jsPatterns = {
                "javascript:", "vbscript:", "data:", "expression(",
                "eval(", "alert(", "confirm(", "prompt(",
                "document.", "window.", "location.", "cookie.",
                "function(", "new function", "settimeout(", "setinterval(",
                "innerhtml", "outerhtml", "insertadjacenthtml",
                "<script", "</script", "&#", "\\u"
        };

        for (String pattern : jsPatterns) {
            if (lowerValue.contains(pattern)) {
                return true;
            }
        }

        // 检查base64编码的潜在脚本
        if (lowerValue.contains("base64")) {
            // 简单的base64解码检查，这里可以根据需要实现更复杂的检测
            return lowerValue.matches(".*base64\\s*,\\s*[a-z0-9+/=]{20,}.*");
        }

        return false;
    }

    /**
     * 检查文本节点是否包含脚本
     */
    private static boolean containsScriptInText(Document doc) {
        NodeList allNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            Node node = allNodes.item(i);

            // 检查元素的文本内容
            String textContent = node.getTextContent();
            if (textContent != null && !textContent.trim().isEmpty()) {
                String lowerText = textContent.toLowerCase();

                // 检查文本中是否包含HTML/XML标签
                if (lowerText.contains("<script") ||
                        lowerText.contains("</script>") ||
                        lowerText.contains("<?") ||
                        lowerText.contains("<!")) {
                    return true;
                }

                // 检查是否包含JavaScript代码
                if (containsJavaScript(lowerText)) {
                    return true;
                }
            }

            // 检查CDATA节点
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                    String cdataContent = child.getTextContent().toLowerCase();
                    if (containsJavaScript(cdataContent) ||
                            cdataContent.contains("<script") ||
                            cdataContent.contains("</script")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    public static FileType getFileType(InputStream is) throws IOException {
        byte[] src = new byte[28];
        is.read(src, 0, 28);
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        FileType[] fileTypes = FileType.values();
        for (FileType fileType : fileTypes) {
            if (stringBuilder.toString().startsWith(fileType.getValue())) {
                return fileType;
            }
        }
        return null;
    }

    private static Boolean isImageCheckType(MultipartFile file) {
        try {
            return getFileType(file.getInputStream()) != null;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return false;
        }
    }

    public static String getImageType(InputStream fileInputStream) {
        byte[] b = new byte[10];
        int l = -1;
        try {
            l = fileInputStream.read(b);
            fileInputStream.close();
        } catch (Exception e) {
            return null;
        }
        if (l == 10) {
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                return "gif";
            } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                return "png";
            } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
                return "jpg";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
