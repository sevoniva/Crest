package io.crest.share.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.crest.api.share.ShareTicketApi;
import io.crest.api.share.request.TicketCreator;
import io.crest.api.share.request.TicketDelRequest;
import io.crest.api.share.request.TicketSwitchRequest;
import io.crest.api.share.vo.TicketVO;
import io.crest.commons.utils.CodingUtil;
import io.crest.share.manage.ShareTicketManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
public class ShareTicketServer implements ShareTicketApi {

    @Resource
    private ShareTicketManage shareTicketManage;

    @Override
    public String saveTicket(TicketCreator creator) {
        return shareTicketManage.saveTicket(creator);
    }

    @Override
    public void deleteTicket(TicketDelRequest request) {
        shareTicketManage.deleteTicket(request);
    }

    @Override
    public void switchRequire(TicketSwitchRequest request) {
        shareTicketManage.switchRequire(request);
    }

    @Override
    public IPage<TicketVO> pager(Long resourceId, int goPage, int pageSize) {
        Page<TicketVO> page = new Page<>(goPage, pageSize);
        return shareTicketManage.query(resourceId, page);
    }

    @Override
    public String tempTicket() {
        return CodingUtil.shortUuid();
    }

    @Override
    public Integer limit() {
        return shareTicketManage.getLimit();
    }
}
