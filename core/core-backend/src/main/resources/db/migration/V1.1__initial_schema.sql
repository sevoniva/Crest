
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自定义触发器存储（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_BLOB_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_BLOB_TRIGGERS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz日历（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_CALENDARS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_CALENDARS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(200) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CronTrigger存储（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_CRON_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_CRON_TRIGGERS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储已经触发的trigger相关信息（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_FIRED_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_FIRED_TRIGGERS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储jobDetails信息（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_JOB_DETAILS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_JOB_DETAILS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz锁表，为多个节点调度提供分布式锁（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_LOCKS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_LOCKS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存放暂停掉的触发器（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储所有节点的scheduler（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SimpleTrigger存储（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int DEFAULT NULL,
  `INT_PROP_2` int DEFAULT NULL,
  `LONG_PROP_1` bigint DEFAULT NULL,
  `LONG_PROP_2` bigint DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint DEFAULT NULL,
  `PREV_FIRE_TIME` bigint DEFAULT NULL,
  `PRIORITY` int DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储定义的trigger（开源作业调度框架Quartz）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `QRTZ_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_TRIGGERS` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `area` (
  `id` varchar(255) NOT NULL COMMENT '区域id,和文件对应',
  `level` varchar(255) DEFAULT NULL COMMENT '区域级别，从高到低country,province,city,district,更细的待定',
  `name` varchar(255) DEFAULT NULL COMMENT '区域名称',
  `pid` varchar(255) NOT NULL COMMENT '父级区域id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='地图区域表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `area` DISABLE KEYS */;
INSERT INTO `area` VALUES ('156','country','中国','000'),('156110000','province','北京市','156'),('156110100','city','北京市','156110000'),('156110101','district','东城区','156110100'),('156110102','district','西城区','156110100'),('156110105','district','朝阳区','156110100'),('156110106','district','丰台区','156110100'),('156110107','district','石景山区','156110100'),('156110108','district','海淀区','156110100'),('156110109','district','门头沟区','156110100'),('156110111','district','房山区','156110100'),('156110112','district','通州区','156110100'),('156110113','district','顺义区','156110100'),('156110114','district','昌平区','156110100'),('156110115','district','大兴区','156110100'),('156110116','district','怀柔区','156110100'),('156110117','district','平谷区','156110100'),('156110118','district','密云区','156110100'),('156110119','district','延庆区','156110100'),('156120000','province','天津市','156'),('156120100','city','天津市','156120000'),('156120101','district','和平区','156120100'),('156120102','district','河 东区','156120100'),('156120103','district','河西区','156120100'),('156120104','district','南开区','156120100'),('156120105','district','河北区','156120100'),('156120106','district','红桥区','156120100'),('156120110','district','东丽区','156120100'),('156120111','district','西青区','156120100'),('156120112','district','津南区','156120100'),('156120113','district','北辰区','156120100'),('156120114','district','武清 区','156120100'),('156120115','district','宝坻区','156120100'),('156120116','district','滨海新区','156120100'),('156120117','district','宁河区','156120100'),('156120118','district','静海区','156120100'),('156120119','district','蓟州区','156120100'),('156130000','province','河北省','156'),('156130100','city','石家庄市','156130000'),('156130102','district','长安区','156130100'),('156130104','district','桥西区','156130100'),('156130105','district','新华区','156130100'),('156130107','district','井陉矿区','156130100'),('156130108','district','裕华区','156130100'),('156130109','district','藁城区','156130100'),('156130110','district','鹿泉区','156130100'),('156130111','district','栾城区','156130100'),('156130121','district','井陉县','156130100'),('156130123','district','正定县','156130100'),('156130125','district','行唐县','156130100'),('156130126','district','灵寿县','156130100'),('156130127','district','高邑县','156130100'),('156130128','district','深泽县','156130100'),('156130129','district','赞皇县','156130100'),('156130130','district','无极县','156130100'),('156130131','district','平山县','156130100'),('156130132','district','元氏县','156130100'),('156130133','district','赵县','156130100'),('156130171','district','石家庄高新技术产业开发区','156130100'),('156130172','district','石家庄循环化工园区','156130100'),('156130181','district','辛集市','156130100'),('156130183','district','晋州市','156130100'),('156130184','district','新乐市','156130100'),('156130200','city','唐山市','156130000'),('156130202','district','路南区','156130200'),('156130203','district','路北区','156130200'),('156130204','district','古冶区','156130200'),('156130205','district','开平区','156130200'),('156130207','district','丰南区','156130200'),('156130208','district','丰润区','156130200'),('156130209','district','曹妃甸区','156130200'),('156130224','district','滦南县','156130200'),('156130225','district','乐亭县','156130200'),('156130227','district','迁西县','156130200'),('156130229','district','玉田县','156130200'),('156130271','district','河北唐山芦台经济开发区','156130200'),('156130272','district','唐山市汉沽管理区','156130200'),('156130273','district','唐山高新技术产业开发区','156130200'),('156130274','district','河北唐山海港经济开发区','156130200'),('156130281','district','遵化市','156130200'),('156130283','district','迁安市','156130200'),('156130284','district','滦州市','156130200'),('156130300','city','秦皇岛市','156130000'),('156130302','district','海港区','156130300'),('156130303','district','山海关区','156130300'),('156130304','district','北戴河区','156130300'),('156130306','district','抚宁区','156130300'),('156130321','district','青龙满族自治县','156130300'),('156130322','district','昌黎县','156130300'),('156130324','district','卢龙县','156130300'),('156130371','district','秦皇岛市经济技术开发区','156130300'),('156130372','district','北戴河新区','156130300'),('156130400','city','邯郸市','156130000'),('156130402','district','邯山区','156130400'),('156130403','district','丛台区','156130400'),('156130404','district','复兴区','156130400'),('156130406','district','峰峰矿区','156130400'),('156130407','district','肥乡区','156130400'),('156130408','district','永年区','156130400'),('156130423','district','临漳县','156130400'),('156130424','district','成安县','156130400'),('156130425','district','大名县','156130400'),('156130426','district','涉县','156130400'),('156130427','district','磁县','156130400'),('156130430','district','邱县','156130400'),('156130431','district','鸡泽县','156130400'),('156130432','district','广平县','156130400'),('156130433','district','馆陶县','156130400'),('156130434','district','魏县','156130400'),('156130435','district','曲周县','156130400'),('156130471','district','邯郸经济技术开发区','156130400'),('156130473','district','邯郸冀南新区','156130400'),('156130481','district','武安市','156130400'),('156130500','city','邢台市','156130000'),('156130502','district','襄都区','156130500'),('156130503','district','信都区','156130500'),('156130505','district','任泽区','156130500'),('156130506','district','南和区','156130500'),('156130522','district','临城县','156130500'),('156130523','district','内丘县','156130500'),('156130524','district','柏乡县','156130500'),('156130525','district','隆尧县','156130500'),('156130528','district','宁晋县','156130500'),('156130529','district','巨鹿县','156130500'),('156130530','district','新河县','156130500'),('156130531','district','广宗县','156130500'),('156130532','district','平乡县','156130500'),('156130533','district','威县','156130500'),('156130534','district','清河县','156130500'),('156130535','district','临西县','156130500'),('156130571','district','河北 邢台经济开发区','156130500'),('156130581','district','南宫市','156130500'),('156130582','district','沙河市','156130500'),('156130600','city','保定市','156130000'),('156130602','district','竞秀区','156130600'),('156130606','district','莲池区','156130600'),('156130607','district','满城区','156130600'),('156130608','district','清苑区','156130600'),('156130609','district','徐水区','156130600'),('156130623','district','涞水县','156130600'),('156130624','district','阜平县','156130600'),('156130626','district','定兴县','156130600'),('156130627','district','唐县','156130600'),('156130628','district','高阳县','156130600'),('156130629','district','容城县','156130600'),('156130630','district','涞源县','156130600'),('156130631','district','望都县','156130600'),('156130632','district','安新县','156130600'),('156130633','district','易县','156130600'),('156130634','district','曲阳县','156130600'),('156130635','district','蠡县','156130600'),('156130636','district','顺平县','156130600'),('156130637','district','博野县','156130600'),('156130638','district','雄县','156130600'),('156130671','district','保定高新技术产业开发区','156130600'),('156130672','district','保定白沟新城','156130600'),('156130681','district','涿州市','156130600'),('156130682','district','定州市','156130600'),('156130683','district','安国市','156130600'),('156130684','district','高碑店市','156130600'),('156130700','city','张家口市','156130000'),('156130702','district','桥东区','156130700'),('156130703','district','桥西区','156130700'),('156130705','district','宣化区','156130700'),('156130706','district','下花园区','156130700'),('156130708','district','万全区','156130700'),('156130709','district','崇礼区','156130700'),('156130722','district','张北县','156130700'),('156130723','district','康保县','156130700'),('156130724','district',' 沽源县','156130700'),('156130725','district','尚义县','156130700'),('156130726','district','蔚县','156130700'),('156130727','district','阳原县','156130700'),('156130728','district','怀安县','156130700'),('156130730','district','怀来县','156130700'),('156130731','district','涿鹿县','156130700'),('156130732','district','赤城县','156130700'),('156130771','district','张家口经济开发区','156130700'),('156130772','district','张家口市察北管理区','156130700'),('156130773','district','张家口市塞北管理区','156130700'),('156130800','city','承德市','156130000'),('156130802','district','双桥区','156130800'),('156130803','district','双滦区','156130800'),('156130804','district','鹰手营子矿区','156130800'),('156130821','district','承德县','156130800'),('156130822','district','兴隆县','156130800'),('156130824','district','滦平县','156130800'),('156130825','district','隆化县','156130800'),('156130826','district','丰宁满族自治县','156130800'),('156130827','district','宽城满族自治县','156130800'),('156130828','district','围场满族蒙古族自治县','156130800'),('156130871','district','承德高新技术产业开发区','156130800'),('156130881','district','平泉市','156130800'),('156130900','city','沧州市','156130000'),('156130902','district','新华区','156130900'),('156130903','district','运河区','156130900'),('156130921','district','沧县','156130900'),('156130922','district','青县','156130900'),('156130923','district','东光县','156130900'),('156130924','district','海兴县','156130900'),('156130925','district','盐山县','156130900'),('156130926','district','肃宁县','156130900'),('156130927','district','南皮县','156130900'),('156130928','district','吴桥县','156130900'),('156130929','district','献县','156130900'),('156130930','district','孟村回族自治县','156130900'),('156130971','district','河北沧州经济开发区','156130900'),('156130972','district','沧州高新技术产业开发区','156130900'),('156130973','district','沧州渤海新区','156130900'),('156130981','district','泊头市','156130900'),('156130982','district','任丘市','156130900'),('156130983','district','黄骅市','156130900'),('156130984','district','河间市','156130900'),('156131000','city','廊坊市','156130000'),('156131002','district','安次区','156131000'),('156131003','district','广阳区','156131000'),('156131022','district','固安县','156131000'),('156131023','district','永清县','156131000'),('156131024','district','香河县','156131000'),('156131025','district','大城县','156131000'),('156131026','district','文安县','156131000'),('156131028','district','大厂回族自治县','156131000'),('156131071','district','廊坊经济技术开发区','156131000'),('156131081','district','霸州市','156131000'),('156131082','district','三河市','156131000'),('156131100','city','衡水市','156130000'),('156131102','district','桃城区','156131100'),('156131103','district','冀州区','156131100'),('156131121','district','枣强县','156131100'),('156131122','district','武邑县','156131100'),('156131123','district','武强县','156131100'),('156131124','district','饶阳县','156131100'),('156131125','district','安平县','156131100'),('156131126','district','故城县','156131100'),('156131127','district','景县','156131100'),('156131128','district','阜城县','156131100'),('156131171','district','河北衡水高新技术产业开发区','156131100'),('156131172','district','衡水滨湖新区','156131100'),('156131182','district','深州市','156131100'),('156140000','province','山西省','156'),('156140100','city','太原市','156140000'),('156140105','district','小店区','156140100'),('156140106','district','迎泽区','156140100'),('156140107','district','杏花岭区','156140100'),('156140108','district','尖草坪区','156140100'),('156140109','district','万柏林区','156140100'),('156140110','district','晋源区','156140100'),('156140121','district','清徐县','156140100'),('156140122','district','阳曲县','156140100'),('156140123','district','娄烦县','156140100'),('156140171','district','山西转型综合改革示范区','156140100'),('156140181','district','古交市','156140100'),('156140200','city','大同市','156140000'),('156140212','district','新荣区','156140200'),('156140213','district','平城区','156140200'),('156140214','district','云冈区','156140200'),('156140215','district','云州区','156140200'),('156140221','district','阳高县','156140200'),('156140222','district','天镇县','156140200'),('156140223','district','广灵县','156140200'),('156140224','district','灵丘县','156140200'),('156140225','district','浑源县','156140200'),('156140226','district','左云县','156140200'),('156140271','district','山西大同经济开发区','156140200'),('156140300','city','阳泉市','156140000'),('156140302','district','城区','156140300'),('156140303','district','矿区','156140300'),('156140311','district','郊区','156140300'),('156140321','district','平定县','156140300'),('156140322','district','盂县','156140300'),('156140400','city','长治市','156140000'),('156140403','district','潞州区','156140400'),('156140404','district','上党 区','156140400'),('156140405','district','屯留区','156140400'),('156140406','district','潞城区','156140400'),('156140423','district','襄垣县','156140400'),('156140425','district','平顺县','156140400'),('156140426','district','黎城县','156140400'),('156140427','district','壶关县','156140400'),('156140428','district','长子县','156140400'),('156140429','district','武乡县','156140400'),('156140430','district','沁县','156140400'),('156140431','district','沁源县','156140400'),('156140471','district','山西长治高新技术产业园区','156140400'),('156140500','city','晋城市','156140000'),('156140502','district','城区','156140500'),('156140521','district','沁水县','156140500'),('156140522','district','阳城县','156140500'),('156140524','district','陵川县','156140500'),('156140525','district','泽州县','156140500'),('156140581','district','高平市','156140500'),('156140600','city','朔州市','156140000'),('156140602','district','朔城区','156140600'),('156140603','district','平鲁区','156140600'),('156140621','district','山阴县','156140600'),('156140622','district','应县','156140600'),('156140623','district','右玉县','156140600'),('156140671','district','山西朔州经济开发区','156140600'),('156140681','district','怀仁市','156140600'),('156140700','city','晋中市','156140000'),('156140702','district','榆次区','156140700'),('156140703','district','太谷区','156140700'),('156140721','district','榆社县','156140700'),('156140722','district','左权县','156140700'),('156140723','district','和顺县','156140700'),('156140724','district','昔阳县','156140700'),('156140725','district','寿阳县','156140700'),('156140727','district','祁县','156140700'),('156140728','district','平遥县','156140700'),('156140729','district','灵石县','156140700'),('156140781','district','介休市','156140700'),('156140800','city','运城市','156140000'),('156140802','district','盐湖区','156140800'),('156140821','district','临猗县','156140800'),('156140822','district','万荣县','156140800'),('156140823','district','闻喜县','156140800'),('156140824','district','稷山县','156140800'),('156140825','district','新绛县','156140800'),('156140826','district','绛县','156140800'),('156140827','district','垣曲县','156140800'),('156140828','district','夏县','156140800'),('156140829','district','平陆县','156140800'),('156140830','district','芮城县','156140800'),('156140881','district','永济市','156140800'),('156140882','district','河津 市','156140800'),('156140900','city','忻州市','156140000'),('156140902','district','忻府区','156140900'),('156140921','district','定襄县','156140900'),('156140922','district','五台县','156140900'),('156140923','district','代县','156140900'),('156140924','district','繁峙县','156140900'),('156140925','district','宁武县','156140900'),('156140926','district','静乐县','156140900'),('156140927','district','神池县','156140900'),('156140928','district','五寨县','156140900'),('156140929','district','岢岚县','156140900'),('156140930','district','河曲县','156140900'),('156140931','district','保德县','156140900'),('156140932','district','偏关县','156140900'),('156140971','district','五台山风景名胜区','156140900'),('156140981','district','原平市','156140900'),('156141000','city','临汾市','156140000'),('156141002','district','尧都区','156141000'),('156141021','district','曲沃县','156141000'),('156141022','district','翼城县','156141000'),('156141023','district','襄汾县','156141000'),('156141024','district','洪洞县','156141000'),('156141025','district','古县','156141000'),('156141026','district','安泽县','156141000'),('156141027','district','浮山县','156141000'),('156141028','district','吉县','156141000'),('156141029','district','乡宁县','156141000'),('156141030','district','大宁县','156141000'),('156141031','district','隰县','156141000'),('156141032','district','永和县','156141000'),('156141033','district','蒲县','156141000'),('156141034','district','汾西县','156141000'),('156141081','district','侯马市','156141000'),('156141082','district','霍州市','156141000'),('156141100','city','吕梁市','156140000'),('156141102','district','离石区','156141100'),('156141121','district','文 水县','156141100'),('156141122','district','交城县','156141100'),('156141123','district','兴县','156141100'),('156141124','district','临县','156141100'),('156141125','district','柳林县','156141100'),('156141126','district','石楼县','156141100'),('156141127','district','岚县','156141100'),('156141128','district','方山县','156141100'),('156141129','district','中阳县','156141100'),('156141130','district','交口县','156141100'),('156141181','district','孝义市','156141100'),('156141182','district','汾阳市','156141100'),('156150000','province','内蒙古自治区','156'),('156150100','city','呼和浩特市','156150000'),('156150102','district','新城区','156150100'),('156150103','district','回民区','156150100'),('156150104','district','玉泉区','156150100'),('156150105','district','赛罕区','156150100'),('156150121','district','土默特左旗','156150100'),('156150122','district','托克托县','156150100'),('156150123','district','和林格尔县','156150100'),('156150124','district','清水河县','156150100'),('156150125','district','武川县','156150100'),('156150172','district','呼和浩特经济技术开发区','156150100'),('156150200','city','包头市','156150000'),('156150202','district','东河区','156150200'),('156150203','district','昆都仑区','156150200'),('156150204','district','青山区','156150200'),('156150205','district','石拐区','156150200'),('156150206','district','白云鄂博矿区','156150200'),('156150207','district','九原区','156150200'),('156150221','district','土默特右旗','156150200'),('156150222','district','固阳县','156150200'),('156150223','district','达尔罕茂明安联合旗','156150200'),('156150271','district','包头稀土高新技术产业开发区','156150200'),('156150300','city','乌海市','156150000'),('156150302','district','海勃湾区','156150300'),('156150303','district','海南区','156150300'),('156150304','district','乌达区','156150300'),('156150400','city','赤峰市','156150000'),('156150402','district','红山区','156150400'),('156150403','district','元宝山区','156150400'),('156150404','district','松山区','156150400'),('156150421','district','阿鲁科尔沁旗','156150400'),('156150422','district','巴林左旗','156150400'),('156150423','district','巴林右旗','156150400'),('156150424','district','林西县','156150400'),('156150425','district','克什克腾旗','156150400'),('156150426','district','翁牛特旗','156150400'),('156150428','district','喀喇沁旗','156150400'),('156150429','district','宁城县','156150400'),('156150430','district','敖汉旗','156150400'),('156150500','city','通辽市','156150000'),('156150502','district','科尔沁区','156150500'),('156150521','district','科尔沁左翼中旗','156150500'),('156150522','district','科尔沁左翼后旗','156150500'),('156150523','district','开鲁县','156150500'),('156150524','district','库伦旗','156150500'),('156150525','district','奈曼旗','156150500'),('156150526','district','扎鲁特旗','156150500'),('156150571','district','通辽经济技术开发区','156150500'),('156150581','district','霍林郭勒市','156150500'),('156150600','city','鄂尔多斯市','156150000'),('156150602','district','东胜区','156150600'),('156150603','district','康巴什区','156150600'),('156150621','district','达拉特旗','156150600'),('156150622','district','准格尔旗','156150600'),('156150623','district','鄂托克前旗','156150600'),('156150624','district','鄂托克旗','156150600'),('156150625','district','杭锦旗','156150600'),('156150626','district','乌审旗','156150600'),('156150627','district','伊金霍洛旗','156150600'),('156150700','city','呼伦贝尔市','156150000'),('156150702','district','海拉尔区','156150700'),('156150703','district','扎赉诺尔区','156150700'),('156150721','district','阿荣旗','156150700'),('156150722','district','莫力达瓦达斡尔族自治旗','156150700'),('156150723','district','鄂伦春自治旗','156150700'),('156150724','district','鄂温克族自治旗','156150700'),('156150725','district','陈巴尔虎旗','156150700'),('156150726','district','新巴尔虎左旗','156150700'),('156150727','district','新巴尔虎右旗','156150700'),('156150781','district','满洲里市','156150700'),('156150782','district','牙克石市','156150700'),('156150783','district','扎兰屯市','156150700'),('156150784','district','额尔古纳市','156150700'),('156150785','district','根河市','156150700'),('156150800','city','巴彦淖尔市','156150000'),('156150802','district',' 临河区','156150800'),('156150821','district','五原县','156150800'),('156150822','district','磴口县','156150800'),('156150823','district','乌拉特前旗','156150800'),('156150824','district','乌拉特中旗','156150800'),('156150825','district','乌拉特后旗','156150800'),('156150826','district','杭锦后旗','156150800'),('156150900','city','乌兰察布市','156150000'),('156150902','district','集宁区','156150900'),('156150921','district','卓资县','156150900'),('156150922','district','化德县','156150900'),('156150923','district','商都县','156150900'),('156150924','district','兴和县','156150900'),('156150925','district','凉城县','156150900'),('156150926','district','察哈尔右翼前旗','156150900'),('156150927','district','察哈尔右翼中旗','156150900'),('156150928','district','察哈尔右翼后旗','156150900'),('156150929','district','四子王旗','156150900'),('156150981','district','丰镇市','156150900'),('156152200','city','兴安盟','156150000'),('156152201','district','乌兰浩特市','156152200'),('156152202','district','阿尔山市','156152200'),('156152221','district','科尔沁右翼前旗','156152200'),('156152222','district','科尔沁右翼中旗','156152200'),('156152223','district','扎赉特旗','156152200'),('156152224','district','突泉县','156152200'),('156152500','city','锡林郭勒盟','156150000'),('156152501','district','二连浩特市','156152500'),('156152502','district','锡林浩特市','156152500'),('156152522','district','阿巴嘎旗','156152500'),('156152523','district','苏尼特左旗','156152500'),('156152524','district','苏尼特右旗','156152500'),('156152525','district','东乌珠穆沁旗','156152500'),('156152526','district','西乌珠穆沁旗','156152500'),('156152527','district','太仆寺旗','156152500'),('156152528','district','镶黄旗','156152500'),('156152529','district','正镶白旗','156152500'),('156152530','district','正蓝旗','156152500'),('156152531','district','多伦县','156152500'),('156152571','district','乌拉盖管委会','156152500'),('156152900','city','阿拉善盟','156150000'),('156152921','district','阿拉善左旗','156152900'),('156152922','district','阿拉善右旗','156152900'),('156152923','district','额济纳旗','156152900'),('156152971','district','内蒙古阿拉善经济开发区','156152900'),('156210000','province','辽宁省','156'),('156210100','city','沈阳市','156210000'),('156210102','district','和平区','156210100'),('156210103','district','沈河区','156210100'),('156210104','district','大东区','156210100'),('156210105','district','皇姑区','156210100'),('156210106','district','铁西区','156210100'),('156210111','district','苏家屯区','156210100'),('156210112','district','浑南区','156210100'),('156210113','district','沈北新区','156210100'),('156210114','district','于洪区','156210100'),('156210115','district','辽中区','156210100'),('156210123','district','康平县','156210100'),('156210124','district','法库县','156210100'),('156210181','district','新民市','156210100'),('156210200','city','大连市','156210000'),('156210202','district','中山区','156210200'),('156210203','district','西岗区','156210200'),('156210204','district','沙河口区','156210200'),('156210211','district','甘井子区','156210200'),('156210212','district','旅顺口区','156210200'),('156210213','district','金州区','156210200'),('156210214','district','普兰店区','156210200'),('156210224','district','长海县','156210200'),('156210281','district','瓦房店市','156210200'),('156210283','district','庄河市','156210200'),('156210300','city','鞍山市','156210000'),('156210302','district','铁东区','156210300'),('156210303','district','铁西区','156210300'),('156210304','district','立山区','156210300'),('156210311','district','千山区','156210300'),('156210321','district','台安县','156210300'),('156210323','district','岫岩满族自治县','156210300'),('156210381','district','海城市','156210300'),('156210400','city','抚顺市','156210000'),('156210402','district','新抚区','156210400'),('156210403','district','东洲区','156210400'),('156210404','district','望花区','156210400'),('156210411','district','顺城区','156210400'),('156210421','district','抚顺县','156210400'),('156210422','district','新宾满族自治县','156210400'),('156210423','district','清原满族自治县','156210400'),('156210500','city','本溪市','156210000'),('156210502','district','平山区','156210500'),('156210503','district','溪湖区','156210500'),('156210504','district','明山区','156210500'),('156210505','district','南芬区','156210500'),('156210521','district','本溪满族自治县','156210500'),('156210522','district','桓仁满族自治县','156210500'),('156210600','city','丹东市','156210000'),('156210602','district','元宝区','156210600'),('156210603','district','振兴区','156210600'),('156210604','district','振安区','156210600'),('156210624','district','宽甸满族自治县','156210600'),('156210681','district','东港市','156210600'),('156210682','district','凤城市','156210600'),('156210700','city','锦州市','156210000'),('156210702','district','古塔区','156210700'),('156210703','district','凌河区','156210700'),('156210711','district','太和区','156210700'),('156210726','district','黑山县','156210700'),('156210727','district','义县','156210700'),('156210781','district','凌海市','156210700'),('156210782','district','北镇市','156210700'),('156210800','city','营口市','156210000'),('156210802','district','站前区','156210800'),('156210803','district','西市区','156210800'),('156210804','district','鲅鱼圈区','156210800'),('156210811','district','老边区','156210800'),('156210881','district','盖州市','156210800'),('156210882','district','大石桥市','156210800'),('156210900','city','阜新市','156210000'),('156210902','district','海 州区','156210900'),('156210903','district','新邱区','156210900'),('156210904','district','太平区','156210900'),('156210905','district','清河门区','156210900'),('156210911','district',' 细河区','156210900'),('156210921','district','阜新蒙古族自治县','156210900'),('156210922','district','彰武县','156210900'),('156211000','city','辽阳市','156210000'),('156211002','district','白塔区','156211000'),('156211003','district','文圣区','156211000'),('156211004','district','宏伟区','156211000'),('156211005','district','弓长岭区','156211000'),('156211011','district','太子河区','156211000'),('156211021','district','辽阳县','156211000'),('156211081','district','灯塔市','156211000'),('156211100','city','盘锦市','156210000'),('156211102','district','双台子区','156211100'),('156211103','district','兴隆台区','156211100'),('156211104','district','大洼区','156211100'),('156211122','district','盘山县','156211100'),('156211200','city','铁岭市','156210000'),('156211202','district','银州区','156211200'),('156211204','district','清河区','156211200'),('156211221','district','铁岭县','156211200'),('156211223','district','西丰县','156211200'),('156211224','district','昌图县','156211200'),('156211281','district','调兵山市','156211200'),('156211282','district','开原市','156211200'),('156211300','city','朝阳市','156210000'),('156211302','district','双塔区','156211300'),('156211303','district','龙城区','156211300'),('156211321','district','朝阳县','156211300'),('156211322','district','建平县','156211300'),('156211324','district','喀喇沁左翼蒙古族自治县','156211300'),('156211381','district','北票市','156211300'),('156211382','district','凌源市','156211300'),('156211400','city','葫芦岛市','156210000'),('156211402','district','连山区','156211400'),('156211403','district','龙港区','156211400'),('156211404','district','南票区','156211400'),('156211421','district','绥中县','156211400'),('156211422','district','建昌县','156211400'),('156211481','district','兴城市','156211400'),('156220000','province','吉林省','156'),('156220100','city','长春市','156220000'),('156220102','district','南关区','156220100'),('156220103','district','宽城区','156220100'),('156220104','district','朝阳区','156220100'),('156220105','district','二道区','156220100'),('156220106','district','绿园区','156220100'),('156220112','district','双阳区','156220100'),('156220113','district','九台区','156220100'),('156220122','district','农安县','156220100'),('156220171','district','长春经济技术开发区','156220100'),('156220172','district','长春净月高新技术产业开发区','156220100'),('156220173','district','长春高新技术产业开发区','156220100'),('156220174','district','长春汽车经济技术开发区','156220100'),('156220182','district','榆树市','156220100'),('156220183','district','德惠市','156220100'),('156220184','district','公主岭市','156220100'),('156220200','city','吉林市','156220000'),('156220202','district','昌邑区','156220200'),('156220203','district','龙潭区','156220200'),('156220204','district','船营区','156220200'),('156220211','district','丰满区','156220200'),('156220221','district','永吉县','156220200'),('156220271','district','吉林经济开发区','156220200'),('156220272','district','吉林高新技术产业开发区','156220200'),('156220273','district','吉林中国 新加坡食品区','156220200'),('156220281','district','蛟河市','156220200'),('156220282','district','桦甸市','156220200'),('156220283','district','舒兰市','156220200'),('156220284','district','磐石市','156220200'),('156220300','city','四平市','156220000'),('156220302','district','铁西区','156220300'),('156220303','district','铁东区','156220300'),('156220322','district','梨树县','156220300'),('156220323','district','伊通满族自治县','156220300'),('156220382','district','双辽市','156220300'),('156220400','city','辽源市','156220000'),('156220402','district','龙山区','156220400'),('156220403','district','西安区','156220400'),('156220421','district','东丰县','156220400'),('156220422','district','东辽县','156220400'),('156220500','city','通化市','156220000'),('156220502','district','东昌区','156220500'),('156220503','district','二道江区','156220500'),('156220521','district','通化县','156220500'),('156220523','district','辉南 县','156220500'),('156220524','district','柳河县','156220500'),('156220581','district','梅河口市','156220500'),('156220582','district','集安市','156220500'),('156220600','city','白山市','156220000'),('156220602','district','浑江区','156220600'),('156220605','district','江源区','156220600'),('156220621','district','抚松县','156220600'),('156220622','district','靖宇县','156220600'),('156220623','district','长白朝鲜族自治县','156220600'),('156220681','district','临江市','156220600'),('156220700','city','松原市','156220000'),('156220702','district','宁江区','156220700'),('156220721','district','前郭尔罗斯蒙古族自治县','156220700'),('156220722','district','长岭县','156220700'),('156220723','district','乾安县','156220700'),('156220771','district','吉林松原经济开发区','156220700'),('156220781','district','扶余市','156220700'),('156220800','city','白城市','156220000'),('156220802','district','洮北区','156220800'),('156220821','district','镇赉县','156220800'),('156220822','district','通榆县','156220800'),('156220871','district','吉林白城经济开发区','156220800'),('156220881','district','洮南市','156220800'),('156220882','district','大安市','156220800'),('156222400','city','延边朝鲜族自治州','156220000'),('156222401','district','延吉市','156222400'),('156222402','district','图们市','156222400'),('156222403','district','敦化市','156222400'),('156222404','district','珲春市','156222400'),('156222405','district','龙井市','156222400'),('156222406','district','和龙市','156222400'),('156222424','district','汪清县','156222400'),('156222426','district','安图县','156222400'),('156230000','province','黑龙江省','156'),('156230100','city','哈尔滨市','156230000'),('156230102','district','道里区','156230100'),('156230103','district','南岗区','156230100'),('156230104','district','道外区','156230100'),('156230108','district','平房区','156230100'),('156230109','district','松北区','156230100'),('156230110','district','香坊区','156230100'),('156230111','district','呼兰区','156230100'),('156230112','district','阿城区','156230100'),('156230113','district','双城区','156230100'),('156230123','district','依兰县','156230100'),('156230124','district','方正县','156230100'),('156230125','district','宾县','156230100'),('156230126','district','巴彦县','156230100'),('156230127','district','木兰县','156230100'),('156230128','district','通河县','156230100'),('156230129','district','延寿县','156230100'),('156230183','district','尚志市','156230100'),('156230184','district','五常市','156230100'),('156230200','city','齐齐哈尔市','156230000'),('156230202','district','龙沙区','156230200'),('156230203','district','建华区','156230200'),('156230204','district','铁锋区','156230200'),('156230205','district','昂昂溪区','156230200'),('156230206','district','富拉尔基区','156230200'),('156230207','district','碾子山区','156230200'),('156230208','district','梅里斯达斡尔族区','156230200'),('156230221','district','龙江县','156230200'),('156230223','district','依安县','156230200'),('156230224','district','泰来县','156230200'),('156230225','district','甘南县','156230200'),('156230227','district','富裕县','156230200'),('156230229','district','克山县','156230200'),('156230230','district','克东县','156230200'),('156230231','district','拜泉县','156230200'),('156230281','district','讷河市','156230200'),('156230300','city','鸡西市','156230000'),('156230302','district','鸡冠区','156230300'),('156230303','district','恒山区','156230300'),('156230304','district','滴道区','156230300'),('156230305','district','梨树区','156230300'),('156230306','district','城子河区','156230300'),('156230307','district','麻山区','156230300'),('156230321','district','鸡东县','156230300'),('156230381','district','虎林市','156230300'),('156230382','district','密山市','156230300'),('156230400','city','鹤岗市','156230000'),('156230402','district','向阳区','156230400'),('156230403','district','工农区','156230400'),('156230404','district','南山区','156230400'),('156230405','district','兴安区','156230400'),('156230406','district','东山区','156230400'),('156230407','district','兴山区','156230400'),('156230421','district','萝北县','156230400'),('156230422','district','绥滨县','156230400'),('156230500','city','双鸭山市','156230000'),('156230502','district','尖山区','156230500'),('156230503','district','岭 东区','156230500'),('156230505','district','四方台区','156230500'),('156230506','district','宝山区','156230500'),('156230521','district','集贤县','156230500'),('156230522','district','友谊县','156230500'),('156230523','district','宝清县','156230500'),('156230524','district','饶河县','156230500'),('156230600','city','大庆市','156230000'),('156230602','district','萨尔图区','156230600'),('156230603','district','龙凤区','156230600'),('156230604','district','让胡路区','156230600'),('156230605','district','红岗区','156230600'),('156230606','district','大同区','156230600'),('156230621','district','肇州县','156230600'),('156230622','district','肇源县','156230600'),('156230623','district','林甸县','156230600'),('156230624','district','杜尔伯特蒙古族自治县','156230600'),('156230671','district','大庆高新技术产业开发区','156230600'),('156230700','city','伊春市','156230000'),('156230717','district','伊美区','156230700'),('156230718','district','乌翠区','156230700'),('156230719','district','友好区','156230700'),('156230722','district','嘉荫县','156230700'),('156230723','district','汤旺县','156230700'),('156230724','district','丰林县','156230700'),('156230725','district','大箐山县','156230700'),('156230726','district','南岔县','156230700'),('156230751','district','金林区','156230700'),('156230781','district','铁力市','156230700'),('156230800','city','佳木斯市','156230000'),('156230803','district','向阳区','156230800'),('156230804','district','前进区','156230800'),('156230805','district','东风区','156230800'),('156230811','district','郊区','156230800'),('156230822','district','桦南县','156230800'),('156230826','district','桦川县','156230800'),('156230828','district','汤原县','156230800'),('156230881','district','同江市','156230800'),('156230882','district','富锦市','156230800'),('156230883','district','抚远市','156230800'),('156230900','city','七台河市','156230000'),('156230902','district','新兴区','156230900'),('156230903','district','桃山区','156230900'),('156230904','district','茄子河区','156230900'),('156230921','district','勃利县','156230900'),('156231000','city','牡丹江市','156230000'),('156231002','district','东安区','156231000'),('156231003','district','阳明区','156231000'),('156231004','district','爱民区','156231000'),('156231005','district','西安区','156231000'),('156231025','district','林口县','156231000'),('156231071','district','牡丹江经济技术开发区','156231000'),('156231081','district','绥芬河市','156231000'),('156231083','district','海林市','156231000'),('156231084','district','宁安市','156231000'),('156231085','district','穆棱市','156231000'),('156231086','district','东宁市','156231000'),('156231100','city','黑河市','156230000'),('156231102','district','爱辉区','156231100'),('156231123','district','逊克县','156231100'),('156231124','district',' 孙吴县','156231100'),('156231181','district','北安市','156231100'),('156231182','district','五大连池市','156231100'),('156231183','district','嫩江市','156231100'),('156231200','city','绥化市','156230000'),('156231202','district','北林区','156231200'),('156231221','district','望奎县','156231200'),('156231222','district','兰西县','156231200'),('156231223','district','青冈县','156231200'),('156231224','district','庆安县','156231200'),('156231225','district','明水县','156231200'),('156231226','district','绥棱县','156231200'),('156231281','district','安达市','156231200'),('156231282','district','肇东市','156231200'),('156231283','district','海伦市','156231200'),('156232700','city','大兴安岭地区','156230000'),('156232701','district','漠河市','156232700'),('156232721','district','呼玛县','156232700'),('156232722','district','塔河县','156232700'),('156232761','district','加格达奇区','156232700'),('156232762','district','松岭区','156232700'),('156232763','district','新林区','156232700'),('156232764','district','呼中区','156232700'),('156310000','province','上海市','156'),('156310100','city','上海市','156310000'),('156310101','district','黄浦区','156310100'),('156310104','district','徐汇区','156310100'),('156310105','district','长宁区','156310100'),('156310106','district','静安区','156310100'),('156310107','district','普陀区','156310100'),('156310109','district','虹口区','156310100'),('156310110','district','杨浦区','156310100'),('156310112','district','闵行区','156310100'),('156310113','district','宝山区','156310100'),('156310114','district','嘉定区','156310100'),('156310115','district','浦东新区','156310100'),('156310116','district','金山区','156310100'),('156310117','district','松江区','156310100'),('156310118','district','青浦区','156310100'),('156310120','district','奉贤区','156310100'),('156310151','district','崇明区','156310100'),('156320000','province','江苏省','156'),('156320100','city','南京市','156320000'),('156320102','district','玄武区','156320100'),('156320104','district','秦淮区','156320100'),('156320105','district','建邺区','156320100'),('156320106','district','鼓楼区','156320100'),('156320111','district','浦口区','156320100'),('156320113','district','栖霞区','156320100'),('156320114','district','雨花台区','156320100'),('156320115','district','江宁区','156320100'),('156320116','district','六合区','156320100'),('156320117','district','溧水区','156320100'),('156320118','district','高淳区','156320100'),('156320200','city','无锡市','156320000'),('156320205','district','锡山区','156320200'),('156320206','district','惠山区','156320200'),('156320211','district','滨湖区','156320200'),('156320213','district','梁溪区','156320200'),('156320214','district','新吴区','156320200'),('156320281','district','江阴市','156320200'),('156320282','district','宜兴市','156320200'),('156320300','city','徐州市','156320000'),('156320302','district','鼓楼区','156320300'),('156320303','district','云龙区','156320300'),('156320305','district','贾汪区','156320300'),('156320311','district','泉山区','156320300'),('156320312','district','铜山区','156320300'),('156320321','district','丰县','156320300'),('156320322','district','沛县','156320300'),('156320324','district','睢宁县','156320300'),('156320371','district','徐州经济技术开发区','156320300'),('156320381','district','新沂市','156320300'),('156320382','district','邳州市','156320300'),('156320400','city','常州市','156320000'),('156320402','district','天宁区','156320400'),('156320404','district','钟楼区','156320400'),('156320411','district','新北区','156320400'),('156320412','district','武进区','156320400'),('156320413','district','金坛区','156320400'),('156320481','district','溧阳市','156320400'),('156320500','city','苏州市','156320000'),('156320505','district','虎丘区','156320500'),('156320506','district','吴中区','156320500'),('156320507','district','相城区','156320500'),('156320508','district','姑苏区','156320500'),('156320509','district','吴江区','156320500'),('156320581','district','常熟市','156320500'),('156320582','district','张家港市','156320500'),('156320583','district','昆山市','156320500'),('156320585','district','太仓市','156320500'),('156320600','city','南通市','156320000'),('156320602','district','崇川区','156320600'),('156320611','district','港闸区','156320600'),('156320612','district','通州区','156320600'),('156320623','district','如东县','156320600'),('156320671','district','南通经济技术开发区','156320600'),('156320681','district','启东市','156320600'),('156320682','district','如皋市','156320600'),('156320684','district','海门市','156320600'),('156320685','district','海安市','156320600'),('156320700','city','连云港市','156320000'),('156320703','district','连云区','156320700'),('156320706','district','海州区','156320700'),('156320707','district','赣榆区','156320700'),('156320722','district','东海县','156320700'),('156320723','district','灌云县','156320700'),('156320724','district','灌南县','156320700'),('156320771','district','连云港经济技术开发区','156320700'),('156320772','district','连云港高新技术产业开发区','156320700'),('156320800','city','淮安市','156320000'),('156320803','district','淮安区','156320800'),('156320804','district','淮阴区','156320800'),('156320812','district','清江浦区','156320800'),('156320813','district','洪泽区','156320800'),('156320826','district','涟水县','156320800'),('156320830','district','盱眙县','156320800'),('156320831','district','金湖县','156320800'),('156320871','district','淮安经济技术开发区','156320800'),('156320900','city','盐城市','156320000'),('156320902','district','亭湖区','156320900'),('156320903','district','盐都区','156320900'),('156320904','district','大丰区','156320900'),('156320921','district','响水县','156320900'),('156320922','district','滨海县','156320900'),('156320923','district','阜宁县','156320900'),('156320924','district','射阳县','156320900'),('156320925','district','建湖县','156320900'),('156320971','district','盐城经济技术开发区','156320900'),('156320981','district','东台市','156320900'),('156321000','city','扬州市','156320000'),('156321002','district','广陵区','156321000'),('156321003','district','邗江区','156321000'),('156321012','district','江都区','156321000'),('156321023','district','宝应县','156321000'),('156321071','district','扬州经济技术开发区','156321000'),('156321081','district','仪征市','156321000'),('156321084','district','高邮市','156321000'),('156321100','city','镇江市','156320000'),('156321102','district','京口区','156321100'),('156321111','district','润州区','156321100'),('156321112','district','丹徒区','156321100'),('156321171','district','镇江新区','156321100'),('156321181','district','丹阳市','156321100'),('156321182','district','扬中市','156321100'),('156321183','district','句容市','156321100'),('156321200','city','泰州市','156320000'),('156321202','district','海陵 区','156321200'),('156321203','district','高港区','156321200'),('156321204','district','姜堰区','156321200'),('156321271','district','泰州医药高新技术产业开发区','156321200'),('156321281','district','兴化市','156321200'),('156321282','district','靖江市','156321200'),('156321283','district','泰兴市','156321200'),('156321300','city','宿迁市','156320000'),('156321302','district','宿城区','156321300'),('156321311','district','宿豫区','156321300'),('156321322','district','沭阳县','156321300'),('156321323','district','泗阳 县','156321300'),('156321324','district','泗洪县','156321300'),('156321371','district','宿迁经济技术开发区','156321300'),('156330000','province','浙江省','156'),('156330100','city','杭州市','156330000'),('156330102','district','上城区','156330100'),('156330105','district','拱墅区','156330100'),('156330106','district','西湖区','156330100'),('156330108','district','滨江区','156330100'),('156330109','district','萧山区','156330100'),('156330110','district','余杭区','156330100'),('156330111','district','富阳区','156330100'),('156330112','district','临安区','156330100'),('156330113','district','临平区','156330100'),('156330114','district','钱塘区','156330100'),('156330122','district','桐庐县','156330100'),('156330127','district','淳安县','156330100'),('156330182','district','建德市','156330100'),('156330200','city','宁波市','156330000'),('156330203','district','海曙区','156330200'),('156330205','district','江北区','156330200'),('156330206','district','北仑区','156330200'),('156330211','district','镇海区','156330200'),('156330212','district','鄞州区','156330200'),('156330213','district','奉化区','156330200'),('156330225','district','象山县','156330200'),('156330226','district','宁海县','156330200'),('156330281','district','余姚市','156330200'),('156330282','district','慈溪市','156330200'),('156330300','city','温州市','156330000'),('156330302','district','鹿城区','156330300'),('156330303','district','龙湾区','156330300'),('156330304','district','瓯海区','156330300'),('156330305','district','洞头区','156330300'),('156330324','district','永嘉县','156330300'),('156330326','district','平阳县','156330300'),('156330327','district','苍南县','156330300'),('156330328','district','文成县','156330300'),('156330329','district','泰顺县','156330300'),('156330371','district','温州经济技术开发区','156330300'),('156330381','district','瑞安市','156330300'),('156330382','district','乐清市','156330300'),('156330383','district','龙港市','156330300'),('156330400','city','嘉兴市','156330000'),('156330402','district','南湖区','156330400'),('156330411','district','秀洲区','156330400'),('156330421','district','嘉善县','156330400'),('156330424','district','海盐县','156330400'),('156330481','district','海宁市','156330400'),('156330482','district','平湖市','156330400'),('156330483','district','桐乡市','156330400'),('156330500','city','湖州市','156330000'),('156330502','district','吴兴区','156330500'),('156330503','district','南浔区','156330500'),('156330521','district','德清县','156330500'),('156330522','district','长兴县','156330500'),('156330523','district','安吉县','156330500'),('156330600','city','绍兴市','156330000'),('156330602','district','越城区','156330600'),('156330603','district','柯桥区','156330600'),('156330604','district','上虞区','156330600'),('156330624','district','新昌县','156330600'),('156330681','district','诸暨市','156330600'),('156330683','district','嵊州市','156330600'),('156330700','city','金华市','156330000'),('156330702','district','婺城区','156330700'),('156330703','district','金东区','156330700'),('156330723','district','武义县','156330700'),('156330726','district','浦江县','156330700'),('156330727','district','磐安县','156330700'),('156330781','district','兰溪市','156330700'),('156330782','district','义乌市','156330700'),('156330783','district','东阳市','156330700'),('156330784','district','永康市','156330700'),('156330800','city','衢州市','156330000'),('156330802','district','柯城区','156330800'),('156330803','district','衢江区','156330800'),('156330822','district','常山县','156330800'),('156330824','district','开化县','156330800'),('156330825','district','龙游县','156330800'),('156330881','district','江山市','156330800'),('156330900','city','舟山市','156330000'),('156330902','district','定海区','156330900'),('156330903','district','普陀区','156330900'),('156330921','district','岱山县','156330900'),('156330922','district','嵊泗县','156330900'),('156331000','city','台州市','156330000'),('156331002','district','椒江区','156331000'),('156331003','district','黄岩区','156331000'),('156331004','district','路桥区','156331000'),('156331022','district','三门县','156331000'),('156331023','district','天台县','156331000'),('156331024','district','仙居县','156331000'),('156331081','district','温岭市','156331000'),('156331082','district','临海市','156331000'),('156331083','district','玉环市','156331000'),('156331100','city','丽水市','156330000'),('156331102','district','莲都区','156331100'),('156331121','district','青田县','156331100'),('156331122','district','缙云 县','156331100'),('156331123','district','遂昌县','156331100'),('156331124','district','松阳县','156331100'),('156331125','district','云和县','156331100'),('156331126','district','庆元县','156331100'),('156331127','district','景宁畲族自治县','156331100'),('156331181','district','龙泉市','156331100'),('156340000','province','安徽省','156'),('156340100','city','合肥市','156340000'),('156340102','district','瑶海区','156340100'),('156340103','district','庐阳区','156340100'),('156340104','district','蜀山区','156340100'),('156340111','district','包河区','156340100'),('156340121','district','长丰县','156340100'),('156340122','district','肥东县','156340100'),('156340123','district','肥西县','156340100'),('156340124','district','庐江县','156340100'),('156340171','district','合肥高新技术产业开发区','156340100'),('156340172','district','合肥经济技术开发区','156340100'),('156340173','district','合肥新站高新技术产业开发区','156340100'),('156340181','district','巢湖市','156340100'),('156340200','city','芜湖市','156340000'),('156340202','district','镜湖区','156340200'),('156340203','district','弋江区','156340200'),('156340207','district','鸠江区','156340200'),('156340208','district','三山区','156340200'),('156340221','district','芜湖县','156340200'),('156340222','district','繁昌县','156340200'),('156340223','district','南陵县','156340200'),('156340271','district','芜湖经济技术开发区','156340200'),('156340272','district','安徽芜湖长江大桥经济开发区','156340200'),('156340281','district','无为市','156340200'),('156340300','city','蚌埠市','156340000'),('156340302','district','龙子湖区','156340300'),('156340303','district','蚌山区','156340300'),('156340304','district','禹会区','156340300'),('156340311','district','淮上区','156340300'),('156340321','district','怀远县','156340300'),('156340322','district','五河县','156340300'),('156340323','district','固镇县','156340300'),('156340371','district','蚌埠市高新技术开发区','156340300'),('156340372','district','蚌埠市经济开发区','156340300'),('156340400','city','淮南市','156340000'),('156340402','district','大通区','156340400'),('156340403','district','田家庵区','156340400'),('156340404','district','谢家集区','156340400'),('156340405','district','八公山区','156340400'),('156340406','district','潘集区','156340400'),('156340421','district','凤台县','156340400'),('156340422','district','寿县','156340400'),('156340500','city','马鞍山市','156340000'),('156340503','district','花山区','156340500'),('156340504','district','雨山区','156340500'),('156340506','district','博望区','156340500'),('156340521','district','当涂县','156340500'),('156340522','district','含山 县','156340500'),('156340523','district','和县','156340500'),('156340600','city','淮北市','156340000'),('156340602','district','杜集区','156340600'),('156340603','district','相山区','156340600'),('156340604','district','烈山区','156340600'),('156340621','district','濉溪县','156340600'),('156340700','city','铜陵市','156340000'),('156340705','district','铜官 区','156340700'),('156340706','district','义安区','156340700'),('156340711','district','郊区','156340700'),('156340722','district','枞阳县','156340700'),('156340800','city','安庆市','156340000'),('156340802','district','迎江区','156340800'),('156340803','district','大观区','156340800'),('156340811','district','宜秀区','156340800'),('156340822','district','怀宁县','156340800'),('156340825','district','太湖县','156340800'),('156340826','district','宿松县','156340800'),('156340827','district','望江县','156340800'),('156340828','district','岳西县','156340800'),('156340871','district','安徽安庆经济开发区','156340800'),('156340881','district','桐城市','156340800'),('156340882','district','潜山市','156340800'),('156341000','city','黄山市','156340000'),('156341002','district','屯溪区','156341000'),('156341003','district','黄山区','156341000'),('156341004','district','徽州区','156341000'),('156341021','district','歙县','156341000'),('156341022','district','休宁县','156341000'),('156341023','district','黟县','156341000'),('156341024','district','祁门县','156341000'),('156341100','city','滁州市','156340000'),('156341102','district','琅琊区','156341100'),('156341103','district','南谯区','156341100'),('156341122','district','来安县','156341100'),('156341124','district','全椒县','156341100'),('156341125','district','定远县','156341100'),('156341126','district','凤阳县','156341100'),('156341171','district','苏滁现代产业园','156341100'),('156341172','district','滁州经济技术开发区','156341100'),('156341181','district','天长市','156341100'),('156341182','district','明光市','156341100'),('156341200','city','阜阳市','156340000'),('156341202','district','颍州区','156341200'),('156341203','district','颍东区','156341200'),('156341204','district','颍泉区','156341200'),('156341221','district','临泉县','156341200'),('156341222','district','太和县','156341200'),('156341225','district','阜南县','156341200'),('156341226','district','颍上县','156341200'),('156341271','district','阜阳合肥现代产业园区','156341200'),('156341272','district','阜阳经济技术开发区','156341200'),('156341282','district','界 首市','156341200'),('156341300','city','宿州市','156340000'),('156341302','district','埇桥区','156341300'),('156341321','district','砀山县','156341300'),('156341322','district','萧县','156341300'),('156341323','district','灵璧县','156341300'),('156341324','district','泗县','156341300'),('156341371','district','宿州马鞍山现代产业园区','156341300'),('156341372','district','宿州经济技术开发区','156341300'),('156341500','city','六安市','156340000'),('156341502','district','金安区','156341500'),('156341503','district','裕安区','156341500'),('156341504','district','叶集区','156341500'),('156341522','district','霍邱县','156341500'),('156341523','district','舒城县','156341500'),('156341524','district','金寨县','156341500'),('156341525','district','霍山县','156341500'),('156341600','city','亳州市','156340000'),('156341602','district','谯城区','156341600'),('156341621','district','涡阳县','156341600'),('156341622','district','蒙城县','156341600'),('156341623','district','利辛县','156341600'),('156341700','city','池州市','156340000'),('156341702','district','贵池区','156341700'),('156341721','district','东至县','156341700'),('156341722','district','石台县','156341700'),('156341723','district','青阳县','156341700'),('156341800','city','宣城市','156340000'),('156341802','district','宣州区','156341800'),('156341821','district','郎溪县','156341800'),('156341823','district','泾县','156341800'),('156341824','district','绩溪县','156341800'),('156341825','district','旌德县','156341800'),('156341871','district','宣城市经济开发区','156341800'),('156341881','district','宁国市','156341800'),('156341882','district','广德市','156341800'),('156350000','province','福建省','156'),('156350100','city','福州市','156350000'),('156350102','district','鼓楼区','156350100'),('156350103','district','台江区','156350100'),('156350104','district','仓山区','156350100'),('156350105','district','马尾区','156350100'),('156350111','district','晋安区','156350100'),('156350112','district','长乐区','156350100'),('156350121','district','闽侯县','156350100'),('156350122','district','连江县','156350100'),('156350123','district','罗源县','156350100'),('156350124','district','闽清县','156350100'),('156350125','district','永泰县','156350100'),('156350128','district','平潭县','156350100'),('156350181','district','福清市','156350100'),('156350200','city','厦门市','156350000'),('156350203','district','思明区','156350200'),('156350205','district','海沧区','156350200'),('156350206','district','湖里区','156350200'),('156350211','district','集美区','156350200'),('156350212','district','同安区','156350200'),('156350213','district','翔安区','156350200'),('156350300','city','莆田市','156350000'),('156350302','district','城厢区','156350300'),('156350303','district','涵江区','156350300'),('156350304','district','荔城区','156350300'),('156350305','district','秀屿区','156350300'),('156350322','district','仙游县','156350300'),('156350400','city','三明市','156350000'),('156350402','district','梅列区','156350400'),('156350403','district','三元区','156350400'),('156350421','district','明溪县','156350400'),('156350423','district','清流县','156350400'),('156350424','district','宁化县','156350400'),('156350425','district','大田县','156350400'),('156350426','district','尤溪县','156350400'),('156350427','district','沙县','156350400'),('156350428','district','将乐县','156350400'),('156350429','district','泰宁县','156350400'),('156350430','district','建宁县','156350400'),('156350481','district','永安市','156350400'),('156350500','city','泉州市','156350000'),('156350502','district','鲤城区','156350500'),('156350503','district','丰泽区','156350500'),('156350504','district','洛江区','156350500'),('156350505','district','泉港区','156350500'),('156350521','district','惠安县','156350500'),('156350524','district','安溪县','156350500'),('156350525','district','永春县','156350500'),('156350526','district','德化县','156350500'),('156350527','district','金门县','156350500'),('156350581','district','石狮市','156350500'),('156350582','district','晋江市','156350500'),('156350583','district','南安市','156350500'),('156350600','city','漳州市','156350000'),('156350602','district','芗城区','156350600'),('156350603','district','龙文区','156350600'),('156350622','district','云霄县','156350600'),('156350623','district','漳浦县','156350600'),('156350624','district','诏安县','156350600'),('156350625','district','长泰县','156350600'),('156350626','district','东山县','156350600'),('156350627','district','南靖县','156350600'),('156350628','district','平和县','156350600'),('156350629','district','华安县','156350600'),('156350681','district','龙海市','156350600'),('156350700','city','南平市','156350000'),('156350702','district','延平区','156350700'),('156350703','district',' 建阳区','156350700'),('156350721','district','顺昌县','156350700'),('156350722','district','浦城县','156350700'),('156350723','district','光泽县','156350700'),('156350724','district','松溪县','156350700'),('156350725','district','政和县','156350700'),('156350781','district','邵武市','156350700'),('156350782','district','武夷山市','156350700'),('156350783','district','建瓯市','156350700'),('156350800','city','龙岩市','156350000'),('156350802','district','新罗区','156350800'),('156350803','district','永定 区','156350800'),('156350821','district','长汀县','156350800'),('156350823','district','上杭县','156350800'),('156350824','district','武平县','156350800'),('156350825','district','连城县','156350800'),('156350881','district','漳平市','156350800'),('156350900','city','宁德市','156350000'),('156350902','district','蕉城区','156350900'),('156350921','district','霞浦县','156350900'),('156350922','district','古田县','156350900'),('156350923','district','屏南县','156350900'),('156350924','district','寿宁县','156350900'),('156350925','district','周宁县','156350900'),('156350926','district','柘荣县','156350900'),('156350981','district','福安市','156350900'),('156350982','district','福鼎市','156350900'),('156360000','province','江西省','156'),('156360100','city','南昌市','156360000'),('156360102','district','东湖区','156360100'),('156360103','district','西湖区','156360100'),('156360104','district','青云谱区','156360100'),('156360111','district','青山湖区','156360100'),('156360112','district','新建区','156360100'),('156360113','district','红谷滩区','156360100'),('156360121','district','南昌县','156360100'),('156360123','district','安义县','156360100'),('156360124','district','进贤县','156360100'),('156360200','city','景德镇市','156360000'),('156360202','district','昌江区','156360200'),('156360203','district','珠山区','156360200'),('156360222','district','浮梁县','156360200'),('156360281','district','乐平市','156360200'),('156360300','city','萍乡市','156360000'),('156360302','district','安源区','156360300'),('156360313','district','湘东区','156360300'),('156360321','district','莲花县','156360300'),('156360322','district','上栗县','156360300'),('156360323','district','芦溪县','156360300'),('156360400','city','九江市','156360000'),('156360402','district','濂溪区','156360400'),('156360403','district','浔阳区','156360400'),('156360404','district','柴桑区','156360400'),('156360423','district','武宁县','156360400'),('156360424','district','修水县','156360400'),('156360425','district','永修县','156360400'),('156360426','district','德安县','156360400'),('156360428','district','都昌县','156360400'),('156360429','district','湖口县','156360400'),('156360430','district','彭泽县','156360400'),('156360481','district','瑞昌市','156360400'),('156360482','district','共青城市','156360400'),('156360483','district','庐山市','156360400'),('156360500','city','新余市','156360000'),('156360502','district','渝水区','156360500'),('156360521','district','分宜县','156360500'),('156360600','city','鹰潭市','156360000'),('156360602','district','月湖区','156360600'),('156360603','district','余江区','156360600'),('156360681','district','贵溪市','156360600'),('156360700','city','赣州市','156360000'),('156360702','district','章贡区','156360700'),('156360703','district','南康区','156360700'),('156360704','district','赣县区','156360700'),('156360722','district','信丰县','156360700'),('156360723','district','大余县','156360700'),('156360724','district','上犹县','156360700'),('156360725','district','崇义县','156360700'),('156360726','district','安远县','156360700'),('156360728','district','定南县','156360700'),('156360729','district','全南县','156360700'),('156360730','district','宁都县','156360700'),('156360731','district','于都县','156360700'),('156360732','district','兴国县','156360700'),('156360733','district','会昌县','156360700'),('156360734','district','寻乌县','156360700'),('156360735','district','石城县','156360700'),('156360781','district','瑞金市','156360700'),('156360783','district','龙南市','156360700'),('156360800','city','吉安市','156360000'),('156360802','district','吉州区','156360800'),('156360803','district','青原区','156360800'),('156360821','district','吉安县','156360800'),('156360822','district','吉水县','156360800'),('156360823','district','峡江县','156360800'),('156360824','district','新干县','156360800'),('156360825','district','永丰县','156360800'),('156360826','district','泰和县','156360800'),('156360827','district','遂川县','156360800'),('156360828','district','万安县','156360800'),('156360829','district','安福县','156360800'),('156360830','district','永新县','156360800'),('156360881','district','井冈山市','156360800'),('156360900','city','宜春市','156360000'),('156360902','district','袁州区','156360900'),('156360921','district','奉新县','156360900'),('156360922','district','万载县','156360900'),('156360923','district','上高 县','156360900'),('156360924','district','宜丰县','156360900'),('156360925','district','靖安县','156360900'),('156360926','district','铜鼓县','156360900'),('156360981','district','丰城市','156360900'),('156360982','district','樟树市','156360900'),('156360983','district','高安市','156360900'),('156361000','city','抚州市','156360000'),('156361002','district','临川区','156361000'),('156361003','district','东乡区','156361000'),('156361021','district','南城县','156361000'),('156361022','district','黎川县','156361000'),('156361023','district','南丰县','156361000'),('156361024','district','崇仁县','156361000'),('156361025','district','乐安县','156361000'),('156361026','district','宜黄县','156361000'),('156361027','district','金溪县','156361000'),('156361028','district','资溪县','156361000'),('156361030','district','广昌县','156361000'),('156361100','city','上饶市','156360000'),('156361102','district','信州区','156361100'),('156361103','district','广丰区','156361100'),('156361104','district','广信区','156361100'),('156361123','district','玉山县','156361100'),('156361124','district','铅山县','156361100'),('156361125','district','横峰县','156361100'),('156361126','district','弋阳县','156361100'),('156361127','district','余干县','156361100'),('156361128','district','鄱阳县','156361100'),('156361129','district','万年县','156361100'),('156361130','district','婺源县','156361100'),('156361181','district','德兴市','156361100'),('156370000','province','山东省','156'),('156370100','city','济南市','156370000'),('156370102','district','历下区','156370100'),('156370103','district','市中区','156370100'),('156370104','district','槐荫区','156370100'),('156370105','district','天桥 区','156370100'),('156370112','district','历城区','156370100'),('156370113','district','长清区','156370100'),('156370114','district','章丘区','156370100'),('156370115','district','济阳区','156370100'),('156370116','district','莱芜区','156370100'),('156370117','district','钢城区','156370100'),('156370124','district','平阴县','156370100'),('156370126','district','商河县','156370100'),('156370171','district','济南高新技术产业开发区','156370100'),('156370200','city','青岛市','156370000'),('156370202','district','市南区','156370200'),('156370203','district','市北区','156370200'),('156370211','district','黄岛区','156370200'),('156370212','district','崂山区','156370200'),('156370213','district','李沧区','156370200'),('156370214','district','城阳区','156370200'),('156370215','district','即墨区','156370200'),('156370271','district','青岛高新技术产业开发区','156370200'),('156370281','district','胶州市','156370200'),('156370283','district','平度 市','156370200'),('156370285','district','莱西市','156370200'),('156370300','city','淄博市','156370000'),('156370302','district','淄川区','156370300'),('156370303','district','张店区','156370300'),('156370304','district','博山区','156370300'),('156370305','district','临淄区','156370300'),('156370306','district','周村区','156370300'),('156370321','district','桓台 县','156370300'),('156370322','district','高青县','156370300'),('156370323','district','沂源县','156370300'),('156370400','city','枣庄市','156370000'),('156370402','district','市中区','156370400'),('156370403','district','薛城区','156370400'),('156370404','district','峄城区','156370400'),('156370405','district','台儿庄区','156370400'),('156370406','district','山亭区','156370400'),('156370481','district','滕州市','156370400'),('156370500','city','东营市','156370000'),('156370502','district','东营区','156370500'),('156370503','district','河口区','156370500'),('156370505','district','垦利区','156370500'),('156370522','district','利津县','156370500'),('156370523','district','广饶县','156370500'),('156370571','district','东营经济技术开发区','156370500'),('156370572','district','东营港经济开发区','156370500'),('156370600','city','烟台市','156370000'),('156370602','district','芝罘区','156370600'),('156370611','district','福山区','156370600'),('156370612','district','牟平区','156370600'),('156370613','district','莱山区','156370600'),('156370614','district','蓬莱区','156370600'),('156370671','district','烟台高新技术产业开发区','156370600'),('156370672','district','烟台经济技术开发区','156370600'),('156370681','district','龙口市','156370600'),('156370682','district','莱阳市','156370600'),('156370683','district','莱州市','156370600'),('156370685','district','招远市','156370600'),('156370686','district','栖霞市','156370600'),('156370687','district','海阳市','156370600'),('156370700','city','潍坊市','156370000'),('156370702','district','潍城区','156370700'),('156370703','district','寒亭区','156370700'),('156370704','district','坊子区','156370700'),('156370705','district','奎文区','156370700'),('156370724','district','临朐县','156370700'),('156370725','district','昌乐县','156370700'),('156370772','district','潍坊滨海经济技术开发区','156370700'),('156370781','district','青州市','156370700'),('156370782','district','诸城市','156370700'),('156370783','district','寿光市','156370700'),('156370784','district','安丘市','156370700'),('156370785','district','高密市','156370700'),('156370786','district','昌邑市','156370700'),('156370800','city','济宁市','156370000'),('156370811','district','任城区','156370800'),('156370812','district','兖州区','156370800'),('156370826','district','微山县','156370800'),('156370827','district','鱼台县','156370800'),('156370828','district','金乡县','156370800'),('156370829','district','嘉祥县','156370800'),('156370830','district','汶上县','156370800'),('156370831','district','泗水县','156370800'),('156370832','district','梁山县','156370800'),('156370871','district','济宁高新技术产业开发区','156370800'),('156370881','district','曲阜市','156370800'),('156370883','district','邹城市','156370800'),('156370900','city','泰安市','156370000'),('156370902','district','泰山区','156370900'),('156370911','district','岱岳区','156370900'),('156370921','district','宁阳县','156370900'),('156370923','district','东平县','156370900'),('156370982','district','新泰市','156370900'),('156370983','district','肥城市','156370900'),('156371000','city','威海市','156370000'),('156371002','district','环翠区','156371000'),('156371003','district','文登区','156371000'),('156371071','district','威海火炬高技术产业开发区','156371000'),('156371072','district','威海经济技术开发区','156371000'),('156371073','district','威海临港经济技术开发区','156371000'),('156371082','district','荣成市','156371000'),('156371083','district','乳山市','156371000'),('156371100','city','日照市','156370000'),('156371102','district','东港区','156371100'),('156371103','district','岚山区','156371100'),('156371121','district','五莲县','156371100'),('156371122','district','莒县','156371100'),('156371171','district','日照经济技术开发区','156371100'),('156371300','city','临沂市','156370000'),('156371302','district','兰山区','156371300'),('156371311','district','罗庄区','156371300'),('156371312','district','河东区','156371300'),('156371321','district','沂南县','156371300'),('156371322','district','郯城县','156371300'),('156371323','district','沂水县','156371300'),('156371324','district','兰陵县','156371300'),('156371325','district','费县','156371300'),('156371326','district','平邑县','156371300'),('156371327','district','莒南县','156371300'),('156371328','district','蒙阴县','156371300'),('156371329','district','临沭县','156371300'),('156371371','district',' 临沂高新技术产业开发区','156371300'),('156371400','city','德州市','156370000'),('156371402','district','德城区','156371400'),('156371403','district','陵城区','156371400'),('156371422','district','宁津县','156371400'),('156371423','district','庆云县','156371400'),('156371424','district','临邑县','156371400'),('156371425','district','齐河县','156371400'),('156371426','district','平原县','156371400'),('156371427','district','夏津县','156371400'),('156371428','district','武城县','156371400'),('156371471','district','德州经济技术开发区','156371400'),('156371472','district','德 州运河经济开发区','156371400'),('156371481','district','乐陵市','156371400'),('156371482','district','禹城市','156371400'),('156371500','city','聊城市','156370000'),('156371502','district','东昌府区','156371500'),('156371503','district','茌平区','156371500'),('156371521','district','阳谷县','156371500'),('156371522','district','莘县','156371500'),('156371524','district','东阿县','156371500'),('156371525','district','冠县','156371500'),('156371526','district','高唐县','156371500'),('156371581','district','临清市','156371500'),('156371600','city','滨州市','156370000'),('156371602','district','滨城区','156371600'),('156371603','district','沾化区','156371600'),('156371621','district','惠民县','156371600'),('156371622','district','阳信县','156371600'),('156371623','district','无棣县','156371600'),('156371625','district','博兴县','156371600'),('156371681','district','邹平市','156371600'),('156371700','city','菏泽市','156370000'),('156371702','district','牡丹区','156371700'),('156371703','district','定陶区','156371700'),('156371721','district','曹县','156371700'),('156371722','district','单县','156371700'),('156371723','district','成武县','156371700'),('156371724','district','巨野县','156371700'),('156371725','district','郓城县','156371700'),('156371726','district','鄄城县','156371700'),('156371728','district','东明县','156371700'),('156371771','district','菏泽经济技术开发区','156371700'),('156371772','district','菏泽高新技术开发区','156371700'),('156410000','province','河南省','156'),('156410100','city','郑州市','156410000'),('156410102','district','中原区','156410100'),('156410103','district','二七区','156410100'),('156410104','district','管城回族区','156410100'),('156410105','district','金水区','156410100'),('156410106','district','上街区','156410100'),('156410108','district','惠济区','156410100'),('156410122','district','中牟县','156410100'),('156410171','district','郑州经济技术开发区','156410100'),('156410172','district','郑州高新技术产业开发区','156410100'),('156410173','district','郑州航空港经济综合实验区','156410100'),('156410181','district','巩义市','156410100'),('156410182','district','荥阳市','156410100'),('156410183','district','新密市','156410100'),('156410184','district','新郑市','156410100'),('156410185','district','登封市','156410100'),('156410200','city','开封市','156410000'),('156410202','district','龙亭区','156410200'),('156410203','district','顺河回族区','156410200'),('156410204','district','鼓楼区','156410200'),('156410205','district','禹王台区','156410200'),('156410212','district','祥符区','156410200'),('156410221','district','杞县','156410200'),('156410222','district','通许县','156410200'),('156410223','district','尉氏县','156410200'),('156410225','district','兰考县','156410200'),('156410300','city','洛阳市','156410000'),('156410302','district','老城区','156410300'),('156410303','district','西工区','156410300'),('156410304','district','瀍河回族区','156410300'),('156410305','district','涧西区','156410300'),('156410306','district','吉利区','156410300'),('156410311','district','洛龙区','156410300'),('156410322','district','孟津县','156410300'),('156410323','district','新安县','156410300'),('156410324','district','栾川县','156410300'),('156410325','district','嵩县','156410300'),('156410326','district','汝阳县','156410300'),('156410327','district','宜阳县','156410300'),('156410328','district','洛宁县','156410300'),('156410329','district','伊川县','156410300'),('156410371','district','洛阳高新技术产业开发区','156410300'),('156410381','district','偃师市','156410300'),('156410400','city','平顶山市','156410000'),('156410402','district','新华区','156410400'),('156410403','district','卫东区','156410400'),('156410404','district','石龙区','156410400'),('156410411','district','湛河区','156410400'),('156410421','district','宝丰县','156410400'),('156410422','district','叶县','156410400'),('156410423','district','鲁山县','156410400'),('156410425','district','郏县','156410400'),('156410471','district','平顶山高新技术产业开发区','156410400'),('156410472','district','平顶山市城乡一体化示范区','156410400'),('156410481','district','舞钢市','156410400'),('156410482','district','汝州市','156410400'),('156410500','city','安阳市','156410000'),('156410502','district','文峰区','156410500'),('156410503','district','北关区','156410500'),('156410505','district','殷都区','156410500'),('156410506','district','龙安区','156410500'),('156410522','district','安阳县','156410500'),('156410523','district','汤阴县','156410500'),('156410526','district','滑县','156410500'),('156410527','district','内黄县','156410500'),('156410571','district','安阳高新技术产业开发区','156410500'),('156410581','district','林州市','156410500'),('156410600','city','鹤壁市','156410000'),('156410602','district','鹤山区','156410600'),('156410603','district','山城区','156410600'),('156410611','district','淇滨区','156410600'),('156410621','district','浚县','156410600'),('156410622','district','淇县','156410600'),('156410671','district','鹤壁经济技术开发区','156410600'),('156410700','city','新乡市','156410000'),('156410702','district','红旗区','156410700'),('156410703','district','卫滨区','156410700'),('156410704','district','凤泉区','156410700'),('156410711','district','牧野区','156410700'),('156410721','district','新乡县','156410700'),('156410724','district','获 嘉县','156410700'),('156410725','district','原阳县','156410700'),('156410726','district','延津县','156410700'),('156410727','district','封丘县','156410700'),('156410771','district','新乡高新技术产业开发区','156410700'),('156410772','district','新乡经济技术开发区','156410700'),('156410773','district','新乡市平原城乡一体化示范区','156410700'),('156410781','district','卫辉市','156410700'),('156410782','district','辉县市','156410700'),('156410783','district','长垣市','156410700'),('156410800','city','焦作市','156410000'),('156410802','district','解放区','156410800'),('156410803','district','中站区','156410800'),('156410804','district','马村区','156410800'),('156410811','district','山阳区','156410800'),('156410821','district','修武县','156410800'),('156410822','district','博爱县','156410800'),('156410823','district','武陟县','156410800'),('156410825','district','温县','156410800'),('156410871','district','焦作城乡一体化示范区','156410800'),('156410882','district','沁阳市','156410800'),('156410883','district','孟州市','156410800'),('156410900','city','濮阳市','156410000'),('156410902','district','华龙区','156410900'),('156410922','district','清丰县','156410900'),('156410923','district','南乐县','156410900'),('156410926','district','范县','156410900'),('156410927','district','台前县','156410900'),('156410928','district','濮阳县','156410900'),('156410971','district','河南濮阳工业园区','156410900'),('156410972','district','濮阳经济技术开发区','156410900'),('156411000','city','许昌市','156410000'),('156411002','district','魏都区','156411000'),('156411003','district','建安区','156411000'),('156411024','district','鄢陵县','156411000'),('156411025','district','襄城县','156411000'),('156411071','district','许昌经济技术开发区','156411000'),('156411081','district','禹州市','156411000'),('156411082','district','长葛市','156411000'),('156411100','city','漯河市','156410000'),('156411102','district','源汇区','156411100'),('156411103','district','郾城区','156411100'),('156411104','district','召陵区','156411100'),('156411121','district','舞阳县','156411100'),('156411122','district','临颍县','156411100'),('156411171','district','漯河经济技术开发区','156411100'),('156411200','city','三门峡市','156410000'),('156411202','district','湖滨区','156411200'),('156411203','district','陕州区','156411200'),('156411221','district','渑池县','156411200'),('156411224','district','卢氏县','156411200'),('156411271','district','河南三门峡经济开发区','156411200'),('156411281','district','义马市','156411200'),('156411282','district','灵宝市','156411200'),('156411300','city','南阳市','156410000'),('156411302','district','宛城区','156411300'),('156411303','district','卧龙区','156411300'),('156411321','district','南召县','156411300'),('156411322','district','方城县','156411300'),('156411323','district','西峡县','156411300'),('156411324','district','镇平县','156411300'),('156411325','district','内乡县','156411300'),('156411326','district','淅川县','156411300'),('156411327','district','社旗县','156411300'),('156411328','district','唐河县','156411300'),('156411329','district','新野县','156411300'),('156411330','district','桐柏县','156411300'),('156411371','district','南阳高新技术产业开发区','156411300'),('156411372','district','南阳市城乡一体化示范区','156411300'),('156411381','district','邓州市','156411300'),('156411400','city','商丘市','156410000'),('156411402','district','梁园区','156411400'),('156411403','district','睢阳区','156411400'),('156411421','district','民权县','156411400'),('156411422','district','睢县','156411400'),('156411423','district','宁陵县','156411400'),('156411424','district','柘城县','156411400'),('156411425','district','虞城县','156411400'),('156411426','district','夏邑县','156411400'),('156411471','district','豫东综合物流产业聚集区','156411400'),('156411472','district','河南商丘经济开发区','156411400'),('156411481','district','永城市','156411400'),('156411500','city','信阳市','156410000'),('156411502','district','浉河区','156411500'),('156411503','district','平桥区','156411500'),('156411521','district','罗山县','156411500'),('156411522','district','光山县','156411500'),('156411523','district','新县','156411500'),('156411524','district','商城县','156411500'),('156411525','district','固始县','156411500'),('156411526','district','潢川县','156411500'),('156411527','district','淮滨县','156411500'),('156411528','district','息县','156411500'),('156411571','district','信阳高新技术产业开发区','156411500'),('156411600','city','周口市','156410000'),('156411602','district','川汇区','156411600'),('156411603','district','淮阳区','156411600'),('156411621','district','扶沟县','156411600'),('156411622','district','西华县','156411600'),('156411623','district','商水县','156411600'),('156411624','district','沈丘县','156411600'),('156411625','district','郸城县','156411600'),('156411627','district','太康县','156411600'),('156411628','district','鹿邑县','156411600'),('156411671','district','河南周口经济开发区','156411600'),('156411681','district','项城市','156411600'),('156411700','city','驻马店市','156410000'),('156411702','district','驿城区','156411700'),('156411721','district','西平县','156411700'),('156411722','district','上蔡县','156411700'),('156411723','district','平舆县','156411700'),('156411724','district','正阳县','156411700'),('156411725','district','确山县','156411700'),('156411726','district','泌阳县','156411700'),('156411727','district','汝南县','156411700'),('156411728','district','遂平县','156411700'),('156411729','district','新蔡县','156411700'),('156411771','district','河南驻马店经济开发区','156411700'),('156419000','city','省直辖县级行政区划','156410000'),('156419001','district','济源市','156419000'),('156420000','province','湖北省','156'),('156420100','city','武汉市','156420000'),('156420102','district','江岸区','156420100'),('156420103','district','江汉区','156420100'),('156420104','district','硚口区','156420100'),('156420105','district','汉阳区','156420100'),('156420106','district','武昌区','156420100'),('156420107','district','青山区','156420100'),('156420111','district','洪山区','156420100'),('156420112','district','东西湖区','156420100'),('156420113','district','汉南区','156420100'),('156420114','district','蔡甸区','156420100'),('156420115','district','江夏区','156420100'),('156420116','district','黄陂区','156420100'),('156420117','district','新洲区','156420100'),('156420200','city','黄石市','156420000'),('156420202','district','黄石港区','156420200'),('156420203','district','西塞山区','156420200'),('156420204','district','下陆区','156420200'),('156420205','district','铁山区','156420200'),('156420222','district','阳新县','156420200'),('156420281','district','大冶市','156420200'),('156420300','city','十堰市','156420000'),('156420302','district','茅箭区','156420300'),('156420303','district','张湾区','156420300'),('156420304','district','郧阳区','156420300'),('156420322','district','郧西县','156420300'),('156420323','district','竹山县','156420300'),('156420324','district','竹溪县','156420300'),('156420325','district','房县','156420300'),('156420381','district','丹江口市','156420300'),('156420500','city','宜昌市','156420000'),('156420502','district','西陵区','156420500'),('156420503','district','伍家岗区','156420500'),('156420504','district','点军区','156420500'),('156420505','district','猇亭区','156420500'),('156420506','district','夷陵区','156420500'),('156420525','district','远安县','156420500'),('156420526','district','兴山县','156420500'),('156420527','district','秭归县','156420500'),('156420528','district','长阳土家族自治县','156420500'),('156420529','district','五峰土家族自治县','156420500'),('156420581','district','宜都市','156420500'),('156420582','district','当阳市','156420500'),('156420583','district','枝江市','156420500'),('156420600','city','襄阳市','156420000'),('156420602','district','襄城区','156420600'),('156420606','district','樊城区','156420600'),('156420607','district','襄州区','156420600'),('156420624','district','南漳县','156420600'),('156420625','district','谷城县','156420600'),('156420626','district','保康县','156420600'),('156420682','district','老河口市','156420600'),('156420683','district','枣阳市','156420600'),('156420684','district','宜城市','156420600'),('156420700','city','鄂州市','156420000'),('156420702','district','梁子湖区','156420700'),('156420703','district','华容区','156420700'),('156420704','district','鄂城区','156420700'),('156420800','city','荆门市','156420000'),('156420802','district','东宝区','156420800'),('156420804','district','掇刀区','156420800'),('156420822','district','沙洋县','156420800'),('156420881','district','钟祥市','156420800'),('156420882','district','京山市','156420800'),('156420900','city','孝感市','156420000'),('156420902','district','孝南区','156420900'),('156420921','district','孝昌县','156420900'),('156420922','district','大悟县','156420900'),('156420923','district','云梦县','156420900'),('156420981','district','应城市','156420900'),('156420982','district','安陆市','156420900'),('156420984','district','汉川市','156420900'),('156421000','city','荆州市','156420000'),('156421002','district','沙市区','156421000'),('156421003','district','荆州区','156421000'),('156421022','district','公安县','156421000'),('156421023','district','监利县','156421000'),('156421024','district','江陵县','156421000'),('156421071','district','荆州经济技术开发区','156421000'),('156421081','district','石首市','156421000'),('156421083','district','洪湖市','156421000'),('156421087','district','松滋市','156421000'),('156421100','city','黄冈市','156420000'),('156421102','district','黄州区','156421100'),('156421121','district','团风县','156421100'),('156421122','district','红安县','156421100'),('156421123','district','罗田县','156421100'),('156421124','district','英山县','156421100'),('156421125','district','浠水县','156421100'),('156421126','district','蕲春县','156421100'),('156421127','district','黄梅县','156421100'),('156421171','district','龙感湖管理区','156421100'),('156421181','district','麻城市','156421100'),('156421182','district','武穴市','156421100'),('156421200','city','咸宁市','156420000'),('156421202','district','咸安区','156421200'),('156421221','district','嘉鱼县','156421200'),('156421222','district','通城 县','156421200'),('156421223','district','崇阳县','156421200'),('156421224','district','通山县','156421200'),('156421281','district','赤壁市','156421200'),('156421300','city','随州市','156420000'),('156421303','district','曾都区','156421300'),('156421321','district','随县','156421300'),('156421381','district','广水市','156421300'),('156422800','city','恩施土家族苗族自治州','156420000'),('156422801','district','恩施市','156422800'),('156422802','district','利川市','156422800'),('156422822','district','建始县','156422800'),('156422823','district','巴东县','156422800'),('156422825','district','宣恩县','156422800'),('156422826','district','咸丰县','156422800'),('156422827','district','来凤县','156422800'),('156422828','district','鹤峰县','156422800'),('156429000','city','省直辖县级行政区划','156420000'),('156429004','district','仙桃市','156429000'),('156429005','district','潜江市','156429000'),('156429006','district','天门市','156429000'),('156429021','district','神农架林区','156429000'),('156430000','province','湖南省','156'),('156430100','city','长沙市','156430000'),('156430102','district','芙蓉区','156430100'),('156430103','district','天心区','156430100'),('156430104','district','岳麓区','156430100'),('156430105','district','开福区','156430100'),('156430111','district','雨花区','156430100'),('156430112','district','望城区','156430100'),('156430121','district','长沙县','156430100'),('156430181','district','浏阳市','156430100'),('156430182','district','宁乡市','156430100'),('156430200','city','株洲市','156430000'),('156430202','district','荷塘区','156430200'),('156430203','district','芦淞区','156430200'),('156430204','district','石峰区','156430200'),('156430211','district','天元区','156430200'),('156430212','district','渌口区','156430200'),('156430223','district','攸县','156430200'),('156430224','district','茶陵县','156430200'),('156430225','district','炎陵县','156430200'),('156430271','district','云龙示范区','156430200'),('156430281','district','醴陵市','156430200'),('156430300','city','湘潭市','156430000'),('156430302','district','雨湖区','156430300'),('156430304','district','岳塘区','156430300'),('156430321','district','湘潭县','156430300'),('156430371','district','湖南湘潭高新技术产业园区','156430300'),('156430372','district','湘潭昭山示范区','156430300'),('156430373','district','湘潭九华示范区','156430300'),('156430381','district','湘乡市','156430300'),('156430382','district','韶山市','156430300'),('156430400','city','衡阳市','156430000'),('156430405','district','珠晖区','156430400'),('156430406','district','雁峰区','156430400'),('156430407','district','石鼓区','156430400'),('156430408','district','蒸湘区','156430400'),('156430412','district','南岳区','156430400'),('156430421','district','衡阳县','156430400'),('156430422','district','衡南县','156430400'),('156430423','district','衡山县','156430400'),('156430424','district','衡东县','156430400'),('156430426','district','祁东县','156430400'),('156430471','district','衡阳综合保税区','156430400'),('156430472','district','湖南衡阳高新技术产业园区','156430400'),('156430473','district','湖南衡阳松木经济开发区','156430400'),('156430481','district','耒阳市','156430400'),('156430482','district','常宁市','156430400'),('156430500','city','邵阳市','156430000'),('156430502','district','双清区','156430500'),('156430503','district','大祥区','156430500'),('156430511','district','北塔区','156430500'),('156430522','district','新邵县','156430500'),('156430523','district','邵阳县','156430500'),('156430524','district','隆回县','156430500'),('156430525','district','洞口县','156430500'),('156430527','district','绥宁县','156430500'),('156430528','district','新宁县','156430500'),('156430529','district','城步苗族自治县','156430500'),('156430581','district','武冈市','156430500'),('156430582','district','邵东市','156430500'),('156430600','city','岳阳 市','156430000'),('156430602','district','岳阳楼区','156430600'),('156430603','district','云溪区','156430600'),('156430611','district','君山区','156430600'),('156430621','district','岳阳县','156430600'),('156430623','district','华容县','156430600'),('156430624','district','湘阴县','156430600'),('156430626','district','平江县','156430600'),('156430671','district','岳阳市屈原管理区','156430600'),('156430681','district','汨罗市','156430600'),('156430682','district','临湘市','156430600'),('156430700','city','常德市','156430000'),('156430702','district','武陵区','156430700'),('156430703','district','鼎城区','156430700'),('156430721','district','安乡县','156430700'),('156430722','district','汉寿县','156430700'),('156430723','district','澧县','156430700'),('156430724','district','临澧县','156430700'),('156430725','district','桃源县','156430700'),('156430726','district','石门县','156430700'),('156430771','district','常德市西洞庭管理区','156430700'),('156430781','district','津市市','156430700'),('156430800','city','张家界市','156430000'),('156430802','district','永定区','156430800'),('156430811','district','武陵源区','156430800'),('156430821','district','慈利县','156430800'),('156430822','district','桑植县','156430800'),('156430900','city','益阳市','156430000'),('156430902','district','资阳区','156430900'),('156430903','district','赫山区','156430900'),('156430921','district','南县','156430900'),('156430922','district','桃江县','156430900'),('156430923','district','安化县','156430900'),('156430971','district','益阳市大通湖管理区','156430900'),('156430972','district','湖南益阳高新技术产业园区','156430900'),('156430981','district','沅江市','156430900'),('156431000','city','郴州市','156430000'),('156431002','district','北湖区','156431000'),('156431003','district','苏仙区','156431000'),('156431021','district','桂阳县','156431000'),('156431022','district','宜章县','156431000'),('156431023','district','永兴县','156431000'),('156431024','district','嘉禾县','156431000'),('156431025','district','临武县','156431000'),('156431026','district','汝城县','156431000'),('156431027','district','桂东县','156431000'),('156431028','district','安仁县','156431000'),('156431081','district','资兴市','156431000'),('156431100','city','永州市','156430000'),('156431102','district','零陵区','156431100'),('156431103','district','冷水滩区','156431100'),('156431121','district','祁阳县','156431100'),('156431122','district','东安县','156431100'),('156431123','district','双牌县','156431100'),('156431124','district','道县','156431100'),('156431125','district','江永县','156431100'),('156431126','district','宁远县','156431100'),('156431127','district','蓝山县','156431100'),('156431128','district','新田县','156431100'),('156431129','district','江华瑶 族自治县','156431100'),('156431171','district','永州经济技术开发区','156431100'),('156431172','district','永州市金洞管理区','156431100'),('156431173','district','永州市回龙圩管理区','156431100'),('156431200','city','怀化市','156430000'),('156431202','district','鹤城区','156431200'),('156431221','district','中方县','156431200'),('156431222','district','沅陵县','156431200'),('156431223','district','辰溪县','156431200'),('156431224','district','溆浦县','156431200'),('156431225','district','会同县','156431200'),('156431226','district','麻阳苗族自治县','156431200'),('156431227','district','新晃侗族自治县','156431200'),('156431228','district','芷江侗族自治县','156431200'),('156431229','district','靖州苗族侗族自治县','156431200'),('156431230','district','通道侗族自治县','156431200'),('156431271','district','怀化市洪江管理区','156431200'),('156431281','district','洪江市','156431200'),('156431300','city','娄底市','156430000'),('156431302','district','娄星区','156431300'),('156431321','district','双峰县','156431300'),('156431322','district','新化县','156431300'),('156431381','district','冷水江市','156431300'),('156431382','district','涟源市','156431300'),('156433100','city','湘西土家族苗族自治州','156430000'),('156433101','district','吉首市','156433100'),('156433122','district','泸溪县','156433100'),('156433123','district','凤凰县','156433100'),('156433124','district','花垣县','156433100'),('156433125','district','保靖县','156433100'),('156433126','district','古丈县','156433100'),('156433127','district','永顺县','156433100'),('156433130','district','龙山县','156433100'),('156440000','province','广东省','156'),('156440100','city','广州市','156440000'),('156440103','district','荔湾区','156440100'),('156440104','district','越秀区','156440100'),('156440105','district','海珠区','156440100'),('156440106','district','天河区','156440100'),('156440111','district','白云区','156440100'),('156440112','district','黄埔区','156440100'),('156440113','district','番禺区','156440100'),('156440114','district','花都区','156440100'),('156440115','district','南沙区','156440100'),('156440117','district','从化区','156440100'),('156440118','district','增城区','156440100'),('156440200','city','韶关市','156440000'),('156440203','district','武江区','156440200'),('156440204','district','浈江区','156440200'),('156440205','district','曲江区','156440200'),('156440222','district','始兴县','156440200'),('156440224','district','仁化县','156440200'),('156440229','district','翁源县','156440200'),('156440232','district','乳源瑶族自治县','156440200'),('156440233','district','新丰县','156440200'),('156440281','district','乐昌市','156440200'),('156440282','district','南雄市','156440200'),('156440300','city','深圳市','156440000'),('156440303','district','罗湖区','156440300'),('156440304','district','福田区','156440300'),('156440305','district','南山区','156440300'),('156440306','district','宝安区','156440300'),('156440307','district','龙岗区','156440300'),('156440308','district','盐田区','156440300'),('156440309','district',' 龙华区','156440300'),('156440310','district','坪山区','156440300'),('156440311','district','光明区','156440300'),('156440315','district','大鹏新区','156440300'),('156440400','city','珠海市','156440000'),('156440402','district','香洲区','156440400'),('156440403','district','斗门区','156440400'),('156440404','district',' 金湾区','156440400'),('156440500','city','汕头市','156440000'),('156440507','district','龙湖区','156440500'),('156440511','district','金平区','156440500'),('156440512','district','濠江区','156440500'),('156440513','district','潮阳区','156440500'),('156440514','district','潮南区','156440500'),('156440515','district','澄海区','156440500'),('156440523','district','南澳县','156440500'),('156440600','city','佛山市','156440000'),('156440604','district','禅城区','156440600'),('156440605','district','南海区','156440600'),('156440606','district','顺德区','156440600'),('156440607','district','三水区','156440600'),('156440608','district','高明区','156440600'),('156440700','city','江门市','156440000'),('156440703','district','蓬江区','156440700'),('156440704','district','江海区','156440700'),('156440705','district','新会区','156440700'),('156440781','district','台山市','156440700'),('156440783','district','开平市','156440700'),('156440784','district','鹤山市','156440700'),('156440785','district','恩平市','156440700'),('156440800','city','湛江市','156440000'),('156440802','district','赤坎区','156440800'),('156440803','district','霞山区','156440800'),('156440804','district','坡头区','156440800'),('156440811','district','麻章区','156440800'),('156440823','district','遂溪县','156440800'),('156440825','district','徐闻县','156440800'),('156440881','district','廉江市','156440800'),('156440882','district','雷州市','156440800'),('156440883','district','吴川市','156440800'),('156440900','city','茂名市','156440000'),('156440902','district','茂南区','156440900'),('156440904','district','电白区','156440900'),('156440981','district','高州市','156440900'),('156440982','district','化州市','156440900'),('156440983','district','信宜市','156440900'),('156441200','city','肇庆市','156440000'),('156441202','district','端州区','156441200'),('156441203','district','鼎湖区','156441200'),('156441204','district','高要区','156441200'),('156441223','district','广宁县','156441200'),('156441224','district','怀集县','156441200'),('156441225','district','封开县','156441200'),('156441226','district','德庆县','156441200'),('156441284','district','四会市','156441200'),('156441300','city','惠州市','156440000'),('156441302','district','惠城区','156441300'),('156441303','district','惠阳区','156441300'),('156441322','district','博罗县','156441300'),('156441323','district','惠东县','156441300'),('156441324','district','龙门县','156441300'),('156441400','city','梅州市','156440000'),('156441402','district','梅江区','156441400'),('156441403','district','梅县区','156441400'),('156441422','district','大埔县','156441400'),('156441423','district','丰顺县','156441400'),('156441424','district','五华县','156441400'),('156441426','district','平远县','156441400'),('156441427','district','蕉岭县','156441400'),('156441481','district','兴宁市','156441400'),('156441500','city','汕尾市','156440000'),('156441502','district','城区','156441500'),('156441521','district','海丰县','156441500'),('156441523','district','陆河县','156441500'),('156441581','district','陆丰市','156441500'),('156441600','city','河源市','156440000'),('156441602','district','源城区','156441600'),('156441621','district','紫 金县','156441600'),('156441622','district','龙川县','156441600'),('156441623','district','连平县','156441600'),('156441624','district','和平县','156441600'),('156441625','district','东源县','156441600'),('156441700','city','阳江市','156440000'),('156441702','district','江城区','156441700'),('156441704','district','阳东区','156441700'),('156441721','district','阳西 县','156441700'),('156441781','district','阳春市','156441700'),('156441800','city','清远市','156440000'),('156441802','district','清城区','156441800'),('156441803','district','清新区','156441800'),('156441821','district','佛冈县','156441800'),('156441823','district','阳山县','156441800'),('156441825','district','连山壮族瑶族自治县','156441800'),('156441826','district','连南瑶族自治县','156441800'),('156441881','district','英德市','156441800'),('156441882','district','连州市','156441800'),('156441900','city','东莞市','156440000'),('156441901','district',' 东莞市','156441900'),('156442000','city','中山市','156440000'),('156442001','district','中山市','156442000'),('156445100','city','潮州市','156440000'),('156445102','district','湘桥区','156445100'),('156445103','district','潮安区','156445100'),('156445122','district','饶平县','156445100'),('156445200','city','揭阳市','156440000'),('156445202','district','榕城区','156445200'),('156445203','district','揭东区','156445200'),('156445222','district','揭西县','156445200'),('156445224','district','惠来县','156445200'),('156445281','district','普宁市','156445200'),('156445300','city','云浮市','156440000'),('156445302','district','云城区','156445300'),('156445303','district','云安区','156445300'),('156445321','district','新兴县','156445300'),('156445322','district','郁南县','156445300'),('156445381','district','罗定市','156445300'),('156450000','province','广西壮族自治区','156'),('156450100','city','南宁市','156450000'),('156450102','district','兴宁区','156450100'),('156450103','district','青秀区','156450100'),('156450105','district','江南区','156450100'),('156450107','district','西乡塘区','156450100'),('156450108','district','良庆区','156450100'),('156450109','district','邕宁区','156450100'),('156450110','district','武鸣区','156450100'),('156450123','district','隆安县','156450100'),('156450124','district','马山县','156450100'),('156450125','district','上林县','156450100'),('156450126','district','宾阳县','156450100'),('156450127','district','横县','156450100'),('156450200','city','柳州市','156450000'),('156450202','district','城中区','156450200'),('156450203','district','鱼峰区','156450200'),('156450204','district','柳南区','156450200'),('156450205','district','柳北区','156450200'),('156450206','district','柳江区','156450200'),('156450222','district','柳城县','156450200'),('156450223','district','鹿寨县','156450200'),('156450224','district','融安县','156450200'),('156450225','district','融水苗族自治县','156450200'),('156450226','district','三江侗族自治县','156450200'),('156450300','city','桂林市','156450000'),('156450302','district','秀峰区','156450300'),('156450303','district','叠彩区','156450300'),('156450304','district','象山区','156450300'),('156450305','district','七星区','156450300'),('156450311','district','雁山区','156450300'),('156450312','district','临桂区','156450300'),('156450321','district','阳朔县','156450300'),('156450323','district','灵川县','156450300'),('156450324','district','全州县','156450300'),('156450325','district','兴安县','156450300'),('156450326','district','永福县','156450300'),('156450327','district','灌阳县','156450300'),('156450328','district','龙胜各族自治县','156450300'),('156450329','district','资源县','156450300'),('156450330','district','平乐县','156450300'),('156450332','district','恭城瑶族自治县','156450300'),('156450381','district','荔浦市','156450300'),('156450400','city','梧州市','156450000'),('156450403','district','万秀区','156450400'),('156450405','district','长洲区','156450400'),('156450406','district','龙圩区','156450400'),('156450421','district','苍梧县','156450400'),('156450422','district','藤县','156450400'),('156450423','district','蒙 山县','156450400'),('156450481','district','岑溪市','156450400'),('156450500','city','北海市','156450000'),('156450502','district','海城区','156450500'),('156450503','district','银海区','156450500'),('156450512','district','铁山港区','156450500'),('156450521','district','合浦县','156450500'),('156450600','city','防城港市','156450000'),('156450602','district','港口区','156450600'),('156450603','district','防城区','156450600'),('156450621','district','上思县','156450600'),('156450681','district','东兴市','156450600'),('156450700','city','钦州市','156450000'),('156450702','district','钦南区','156450700'),('156450703','district','钦北区','156450700'),('156450721','district','灵山县','156450700'),('156450722','district','浦北县','156450700'),('156450800','city','贵港市','156450000'),('156450802','district','港北区','156450800'),('156450803','district','港南区','156450800'),('156450804','district','覃塘区','156450800'),('156450821','district','平南县','156450800'),('156450881','district','桂平市','156450800'),('156450900','city','玉林市','156450000'),('156450902','district','玉州区','156450900'),('156450903','district','福绵区','156450900'),('156450921','district','容县','156450900'),('156450922','district','陆川县','156450900'),('156450923','district','博白县','156450900'),('156450924','district','兴业县','156450900'),('156450981','district','北流市','156450900'),('156451000','city','百色市','156450000'),('156451002','district','右江区','156451000'),('156451003','district','田阳区','156451000'),('156451022','district','田东县','156451000'),('156451024','district','德保县','156451000'),('156451026','district','那坡县','156451000'),('156451027','district','凌云县','156451000'),('156451028','district','乐业县','156451000'),('156451029','district','田林县','156451000'),('156451030','district','西林县','156451000'),('156451031','district','隆林各族自治县','156451000'),('156451081','district','靖西市','156451000'),('156451082','district','平果市','156451000'),('156451100','city','贺州市','156450000'),('156451102','district',' 八步区','156451100'),('156451103','district','平桂区','156451100'),('156451121','district','昭平县','156451100'),('156451122','district','钟山县','156451100'),('156451123','district','富川瑶族自治县','156451100'),('156451200','city','河池市','156450000'),('156451202','district','金城江区','156451200'),('156451203','district','宜州区','156451200'),('156451221','district','南丹县','156451200'),('156451222','district','天峨县','156451200'),('156451223','district','凤山县','156451200'),('156451224','district','东兰县','156451200'),('156451225','district','罗城仫佬族自治县','156451200'),('156451226','district','环江毛南族自治县','156451200'),('156451227','district','巴马瑶族自治县','156451200'),('156451228','district','都安瑶族自治县','156451200'),('156451229','district','大化瑶族自治县','156451200'),('156451300','city','来宾市','156450000'),('156451302','district','兴宾区','156451300'),('156451321','district','忻城县','156451300'),('156451322','district','象州县','156451300'),('156451323','district','武宣县','156451300'),('156451324','district','金秀瑶族自治县','156451300'),('156451381','district','合山市','156451300'),('156451400','city','崇左市','156450000'),('156451402','district','江州区','156451400'),('156451421','district','扶绥县','156451400'),('156451422','district','宁明县','156451400'),('156451423','district','龙州县','156451400'),('156451424','district','大新县','156451400'),('156451425','district','天等县','156451400'),('156451481','district','凭祥市','156451400'),('156460000','province','海南省','156'),('156460100','city','海口市','156460000'),('156460105','district','秀英区','156460100'),('156460106','district','龙华区','156460100'),('156460107','district','琼山区','156460100'),('156460108','district','美兰区','156460100'),('156460200','city','三亚市','156460000'),('156460202','district','海棠区','156460200'),('156460203','district','吉阳区','156460200'),('156460204','district','天涯区','156460200'),('156460205','district','崖州区','156460200'),('156460300','city','三沙市','156460000'),('156460321','district','西沙群岛','156460300'),('156460322','district','南沙群岛','156460300'),('156460323','district','中沙群岛的岛礁及其海域','156460300'),('156460400','city','儋州市','156460000'),('156460401','district','儋州市','156460400'),('156469000','city','省直辖县级行政区划','156460000'),('156469001','district','五指山市','156469000'),('156469002','district','琼海市','156469000'),('156469005','district','文昌市','156469000'),('156469006','district','万宁市','156469000'),('156469007','district','东方市','156469000'),('156469021','district','定安县','156469000'),('156469022','district','屯昌县','156469000'),('156469023','district','澄迈县','156469000'),('156469024','district','临高县','156469000'),('156469025','district','白沙黎族自治县','156469000'),('156469026','district','昌江黎族自治县','156469000'),('156469027','district','乐东黎族自治县','156469000'),('156469028','district','陵水黎族自治县','156469000'),('156469029','district','保亭黎族苗族自治县','156469000'),('156469030','district','琼中黎族苗族自治县','156469000'),('156500000','province','重庆市','156'),('156500100','city','重庆市','156500000'),('156500101','district','万州区','156500100'),('156500102','district','涪陵区','156500100'),('156500103','district','渝中区','156500100'),('156500104','district','大渡口区','156500100'),('156500105','district','江北区','156500100'),('156500106','district','沙坪坝区','156500100'),('156500107','district','九龙坡区','156500100'),('156500108','district','南岸区','156500100'),('156500109','district','北碚区','156500100'),('156500110','district','綦江区','156500100'),('156500111','district','大足区','156500100'),('156500112','district','渝北区','156500100'),('156500113','district','巴南区','156500100'),('156500114','district','黔江区','156500100'),('156500115','district','长寿区','156500100'),('156500116','district','江津区','156500100'),('156500117','district','合川区','156500100'),('156500118','district','永川区','156500100'),('156500119','district','南川区','156500100'),('156500120','district','璧山区','156500100'),('156500151','district','铜梁区','156500100'),('156500152','district','潼南区','156500100'),('156500153','district','荣昌区','156500100'),('156500154','district','开州区','156500100'),('156500155','district','梁平区','156500100'),('156500156','district','武隆区','156500100'),('156500229','district','城口县','156500100'),('156500230','district','丰都县','156500100'),('156500231','district','垫江县','156500100'),('156500233','district','忠县','156500100'),('156500235','district','云阳县','156500100'),('156500236','district','奉节县','156500100'),('156500237','district','巫山县','156500100'),('156500238','district','巫溪县','156500100'),('156500240','district','石柱土家族自治县','156500100'),('156500241','district','秀山土家族苗族自治县','156500100'),('156500242','district','酉阳土家族苗族自治县','156500100'),('156500243','district','彭水苗族土家族自治县','156500100'),('156510000','province','四川省','156'),('156510100','city','成都市','156510000'),('156510104','district','锦江区','156510100'),('156510105','district','青羊区','156510100'),('156510106','district','金牛区','156510100'),('156510107','district','武侯区','156510100'),('156510108','district','成华区','156510100'),('156510112','district','龙泉驿区','156510100'),('156510113','district','青白江区','156510100'),('156510114','district','新都区','156510100'),('156510115','district','温江区','156510100'),('156510116','district','双流区','156510100'),('156510117','district','郫都区','156510100'),('156510118','district','新津区','156510100'),('156510121','district','金堂县','156510100'),('156510129','district','大邑县','156510100'),('156510131','district','蒲江县','156510100'),('156510181','district','都江堰市','156510100'),('156510182','district','彭州市','156510100'),('156510183','district','邛崃市','156510100'),('156510184','district','崇州市','156510100'),('156510185','district','简阳市','156510100'),('156510300','city','自贡市','156510000'),('156510302','district','自流井区','156510300'),('156510303','district','贡井区','156510300'),('156510304','district','大安区','156510300'),('156510311','district','沿滩区','156510300'),('156510321','district','荣县','156510300'),('156510322','district','富顺县','156510300'),('156510400','city','攀枝花市','156510000'),('156510402','district','东区','156510400'),('156510403','district','西区','156510400'),('156510411','district','仁和区','156510400'),('156510421','district','米易县','156510400'),('156510422','district','盐边县','156510400'),('156510500','city','泸州市','156510000'),('156510502','district','江阳区','156510500'),('156510503','district','纳溪区','156510500'),('156510504','district','龙马潭区','156510500'),('156510521','district','泸县','156510500'),('156510522','district','合江县','156510500'),('156510524','district','叙永县','156510500'),('156510525','district','古蔺县','156510500'),('156510600','city','德阳市','156510000'),('156510603','district','旌阳区','156510600'),('156510604','district','罗江区','156510600'),('156510623','district','中江县','156510600'),('156510681','district','广汉市','156510600'),('156510682','district','什 邡市','156510600'),('156510683','district','绵竹市','156510600'),('156510700','city','绵阳市','156510000'),('156510703','district','涪城区','156510700'),('156510704','district','游仙区','156510700'),('156510705','district','安州区','156510700'),('156510722','district','三台县','156510700'),('156510723','district','盐亭县','156510700'),('156510725','district','梓潼县','156510700'),('156510726','district','北川羌族自治县','156510700'),('156510727','district','平武县','156510700'),('156510781','district','江油市','156510700'),('156510800','city','广元市','156510000'),('156510802','district','利州区','156510800'),('156510811','district','昭化区','156510800'),('156510812','district','朝天区','156510800'),('156510821','district','旺苍县','156510800'),('156510822','district','青川县','156510800'),('156510823','district','剑阁县','156510800'),('156510824','district','苍溪县','156510800'),('156510900','city','遂宁市','156510000'),('156510903','district','船山区','156510900'),('156510904','district','安居区','156510900'),('156510921','district','蓬溪县','156510900'),('156510923','district','大英县','156510900'),('156510981','district','射洪市','156510900'),('156511000','city','内江市','156510000'),('156511002','district','市中区','156511000'),('156511011','district','东兴区','156511000'),('156511024','district','威远县','156511000'),('156511025','district','资中县','156511000'),('156511071','district','内江经济开发区','156511000'),('156511083','district','隆昌市','156511000'),('156511100','city','乐山市','156510000'),('156511102','district','市中区','156511100'),('156511111','district','沙湾区','156511100'),('156511112','district','五通桥区','156511100'),('156511113','district','金口河区','156511100'),('156511123','district','犍为县','156511100'),('156511124','district','井研县','156511100'),('156511126','district','夹江县','156511100'),('156511129','district','沐川县','156511100'),('156511132','district','峨边彝族自治县','156511100'),('156511133','district','马边彝族自治县','156511100'),('156511181','district','峨眉山市','156511100'),('156511300','city','南充市','156510000'),('156511302','district','顺庆区','156511300'),('156511303','district','高坪区','156511300'),('156511304','district','嘉陵区','156511300'),('156511321','district','南部县','156511300'),('156511322','district','营山县','156511300'),('156511323','district','蓬安县','156511300'),('156511324','district','仪陇县','156511300'),('156511325','district','西充县','156511300'),('156511381','district','阆中市','156511300'),('156511400','city','眉山市','156510000'),('156511402','district','东坡区','156511400'),('156511403','district','彭山区','156511400'),('156511421','district','仁寿县','156511400'),('156511423','district','洪雅县','156511400'),('156511424','district','丹棱县','156511400'),('156511425','district','青神县','156511400'),('156511500','city','宜宾市','156510000'),('156511502','district','翠屏区','156511500'),('156511503','district','南溪区','156511500'),('156511504','district','叙州区','156511500'),('156511523','district','江安县','156511500'),('156511524','district','长宁县','156511500'),('156511525','district','高县','156511500'),('156511526','district','珙县','156511500'),('156511527','district','筠连县','156511500'),('156511528','district','兴文县','156511500'),('156511529','district','屏山县','156511500'),('156511600','city','广安市','156510000'),('156511602','district','广安区','156511600'),('156511603','district','前锋区','156511600'),('156511621','district','岳池 县','156511600'),('156511622','district','武胜县','156511600'),('156511623','district','邻水县','156511600'),('156511681','district','华蓥市','156511600'),('156511700','city','达州市','156510000'),('156511702','district','通川区','156511700'),('156511703','district','达川区','156511700'),('156511722','district','宣汉县','156511700'),('156511723','district','开江县','156511700'),('156511724','district','大竹县','156511700'),('156511725','district','渠县','156511700'),('156511771','district','达州经济开发区','156511700'),('156511781','district','万源市','156511700'),('156511800','city','雅安市','156510000'),('156511802','district','雨城区','156511800'),('156511803','district',' 名山区','156511800'),('156511822','district','荥经县','156511800'),('156511823','district','汉源县','156511800'),('156511824','district','石棉县','156511800'),('156511825','district','天全县','156511800'),('156511826','district','芦山县','156511800'),('156511827','district','宝兴县','156511800'),('156511900','city','巴中市','156510000'),('156511902','district','巴州区','156511900'),('156511903','district','恩阳区','156511900'),('156511921','district','通江县','156511900'),('156511922','district','南江县','156511900'),('156511923','district','平昌县','156511900'),('156511971','district','巴中经济开发区','156511900'),('156512000','city','资阳市','156510000'),('156512002','district','雁江区','156512000'),('156512021','district','安岳县','156512000'),('156512022','district','乐至县','156512000'),('156513200','city','阿坝藏族羌族自 治州','156510000'),('156513201','district','马尔康市','156513200'),('156513221','district','汶川县','156513200'),('156513222','district','理县','156513200'),('156513223','district','茂县','156513200'),('156513224','district','松潘县','156513200'),('156513225','district','九寨沟县','156513200'),('156513226','district','金川县','156513200'),('156513227','district','小金县','156513200'),('156513228','district','黑水县','156513200'),('156513230','district','壤塘县','156513200'),('156513231','district','阿坝县','156513200'),('156513232','district','若尔盖县','156513200'),('156513233','district','红原县','156513200'),('156513300','city','甘孜藏族自治州','156510000'),('156513301','district','康定市','156513300'),('156513322','district','泸定县','156513300'),('156513323','district','丹巴县','156513300'),('156513324','district','九龙县','156513300'),('156513325','district','雅江县','156513300'),('156513326','district','道孚县','156513300'),('156513327','district','炉霍县','156513300'),('156513328','district','甘孜县','156513300'),('156513329','district','新龙 县','156513300'),('156513330','district','德格县','156513300'),('156513331','district','白玉县','156513300'),('156513332','district','石渠县','156513300'),('156513333','district','色达县','156513300'),('156513334','district','理塘县','156513300'),('156513335','district','巴塘县','156513300'),('156513336','district','乡城县','156513300'),('156513337','district','稻城县','156513300'),('156513338','district','得荣县','156513300'),('156513400','city','凉山彝族自治州','156510000'),('156513401','district','西昌市','156513400'),('156513422','district','木里藏族自治县','156513400'),('156513423','district','盐源县','156513400'),('156513424','district','德昌县','156513400'),('156513425','district','会理县','156513400'),('156513426','district','会东县','156513400'),('156513427','district','宁南县','156513400'),('156513428','district','普格县','156513400'),('156513429','district','布拖县','156513400'),('156513430','district','金阳县','156513400'),('156513431','district','昭觉县','156513400'),('156513432','district','喜德县','156513400'),('156513433','district','冕宁县','156513400'),('156513434','district','越西县','156513400'),('156513435','district','甘洛县','156513400'),('156513436','district','美姑县','156513400'),('156513437','district','雷波县','156513400'),('156520000','province','贵州省','156'),('156520100','city','贵阳市','156520000'),('156520102','district','南明区','156520100'),('156520103','district','云岩区','156520100'),('156520111','district','花溪区','156520100'),('156520112','district','乌当区','156520100'),('156520113','district','白云区','156520100'),('156520115','district','观山湖区','156520100'),('156520121','district','开阳县','156520100'),('156520122','district','息烽县','156520100'),('156520123','district','修文县','156520100'),('156520181','district','清镇市','156520100'),('156520200','city','六盘水市','156520000'),('156520201','district','钟山区','156520200'),('156520203','district',' 六枝特区','156520200'),('156520221','district','水城县','156520200'),('156520281','district','盘州市','156520200'),('156520300','city','遵义市','156520000'),('156520302','district','红花岗区','156520300'),('156520303','district','汇川区','156520300'),('156520304','district','播州区','156520300'),('156520322','district','桐梓县','156520300'),('156520323','district','绥阳县','156520300'),('156520324','district','正安县','156520300'),('156520325','district','道真仡佬族苗族自治县','156520300'),('156520326','district','务川仡佬族苗族自治县','156520300'),('156520327','district','凤冈县','156520300'),('156520328','district','湄潭县','156520300'),('156520329','district','余庆县','156520300'),('156520330','district','习水县','156520300'),('156520381','district','赤水市','156520300'),('156520382','district','仁怀市','156520300'),('156520400','city','安顺市','156520000'),('156520402','district','西秀区','156520400'),('156520403','district','平坝区','156520400'),('156520422','district','普定县','156520400'),('156520423','district','镇宁布依族苗族自治县','156520400'),('156520424','district','关岭布依族苗族自治县','156520400'),('156520425','district','紫云苗族布依族自治县','156520400'),('156520500','city','毕节市','156520000'),('156520502','district','七星关区','156520500'),('156520521','district','大方县','156520500'),('156520522','district','黔西县','156520500'),('156520523','district','金沙县','156520500'),('156520524','district','织金县','156520500'),('156520525','district','纳雍县','156520500'),('156520526','district','威宁彝族回族苗族自治县','156520500'),('156520527','district','赫章县','156520500'),('156520600','city','铜仁市','156520000'),('156520602','district','碧江区','156520600'),('156520603','district','万山区','156520600'),('156520621','district','江口县','156520600'),('156520622','district','玉屏侗族自治县','156520600'),('156520623','district','石阡县','156520600'),('156520624','district','思南县','156520600'),('156520625','district','印江土家族苗族自 治县','156520600'),('156520626','district','德江县','156520600'),('156520627','district','沿河土家族自治县','156520600'),('156520628','district','松桃苗族自治县','156520600'),('156522300','city','黔西南布依族苗族自治州','156520000'),('156522301','district','兴义市','156522300'),('156522302','district','兴仁市','156522300'),('156522323','district','普安县','156522300'),('156522324','district','晴隆县','156522300'),('156522325','district','贞丰县','156522300'),('156522326','district','望谟县','156522300'),('156522327','district','册亨县','156522300'),('156522328','district','安龙县','156522300'),('156522600','city','黔东南苗族侗族自治州','156520000'),('156522601','district','凯里市','156522600'),('156522622','district','黄平县','156522600'),('156522623','district','施秉县','156522600'),('156522624','district','三穗县','156522600'),('156522625','district','镇远县','156522600'),('156522626','district','岑巩县','156522600'),('156522627','district','天柱县','156522600'),('156522628','district','锦屏县','156522600'),('156522629','district','剑河县','156522600'),('156522630','district','台江县','156522600'),('156522631','district','黎平县','156522600'),('156522632','district','榕江县','156522600'),('156522633','district','从江县','156522600'),('156522634','district','雷山县','156522600'),('156522635','district','麻江县','156522600'),('156522636','district','丹寨县','156522600'),('156522700','city','黔南布依族苗族自治州','156520000'),('156522701','district','都匀市','156522700'),('156522702','district','福泉市','156522700'),('156522722','district','荔波县','156522700'),('156522723','district','贵定县','156522700'),('156522725','district','瓮安县','156522700'),('156522726','district','独山县','156522700'),('156522727','district','平塘县','156522700'),('156522728','district','罗甸县','156522700'),('156522729','district','长顺县','156522700'),('156522730','district','龙里县','156522700'),('156522731','district','惠水县','156522700'),('156522732','district','三都水族自治县','156522700'),('156530000','province','云南省','156'),('156530100','city','昆明市','156530000'),('156530102','district','五华区','156530100'),('156530103','district','盘龙区','156530100'),('156530111','district','官渡区','156530100'),('156530112','district','西 山区','156530100'),('156530113','district','东川区','156530100'),('156530114','district','呈贡区','156530100'),('156530115','district','晋宁区','156530100'),('156530124','district','富民县','156530100'),('156530125','district','宜良县','156530100'),('156530126','district','石林彝族自治县','156530100'),('156530127','district','嵩明县','156530100'),('156530128','district','禄劝彝族苗族自治县','156530100'),('156530129','district','寻甸回族彝族自治县','156530100'),('156530181','district','安宁市','156530100'),('156530300','city','曲靖市','156530000'),('156530302','district','麒麟区','156530300'),('156530303','district','沾益区','156530300'),('156530304','district','马龙区','156530300'),('156530322','district','陆良县','156530300'),('156530323','district','师宗县','156530300'),('156530324','district','罗平县','156530300'),('156530325','district','富源县','156530300'),('156530326','district','会泽县','156530300'),('156530381','district','宣威市','156530300'),('156530400','city','玉溪市','156530000'),('156530402','district','红塔区','156530400'),('156530403','district','江川区','156530400'),('156530423','district','通海县','156530400'),('156530424','district','华宁县','156530400'),('156530425','district','易门县','156530400'),('156530426','district','峨山彝族自治县','156530400'),('156530427','district','新平彝族傣族自治县','156530400'),('156530428','district','元江哈尼族彝族傣族自治县','156530400'),('156530481','district','澄江市','156530400'),('156530500','city','保山市','156530000'),('156530502','district','隆阳区','156530500'),('156530521','district','施甸县','156530500'),('156530523','district','龙陵县','156530500'),('156530524','district','昌宁县','156530500'),('156530581','district','腾冲市','156530500'),('156530600','city','昭通市','156530000'),('156530602','district','昭阳区','156530600'),('156530621','district','鲁甸县','156530600'),('156530622','district','巧家县','156530600'),('156530623','district','盐津县','156530600'),('156530624','district','大关县','156530600'),('156530625','district','永善县','156530600'),('156530626','district','绥江县','156530600'),('156530627','district','镇雄县','156530600'),('156530628','district','彝良县','156530600'),('156530629','district','威信县','156530600'),('156530681','district','水富市','156530600'),('156530700','city','丽江市','156530000'),('156530702','district','古城区','156530700'),('156530721','district','玉龙纳西族自治县','156530700'),('156530722','district','永胜县','156530700'),('156530723','district','华坪县','156530700'),('156530724','district','宁蒗彝族自治县','156530700'),('156530800','city','普洱市','156530000'),('156530802','district','思茅区','156530800'),('156530821','district','宁洱哈尼族彝族自治县','156530800'),('156530822','district','墨江哈尼族自治县','156530800'),('156530823','district','景东彝族自治县','156530800'),('156530824','district','景谷傣族彝族自治县','156530800'),('156530825','district','镇沅彝族哈尼族拉祜族自治县','156530800'),('156530826','district','江城哈尼族彝族自治县','156530800'),('156530827','district','孟连傣族拉祜族佤族自治县','156530800'),('156530828','district','澜沧拉祜族自治县','156530800'),('156530829','district','西盟佤族自治县','156530800'),('156530900','city','临沧市','156530000'),('156530902','district','临翔区','156530900'),('156530921','district','凤庆县','156530900'),('156530922','district','云县','156530900'),('156530923','district',' 永德县','156530900'),('156530924','district','镇康县','156530900'),('156530925','district','双江拉祜族佤族布朗族傣族自治县','156530900'),('156530926','district','耿马傣族佤族自治县','156530900'),('156530927','district','沧源佤族自治县','156530900'),('156532300','city','楚雄彝族自治州','156530000'),('156532301','district','楚雄市','156532300'),('156532322','district','双柏县','156532300'),('156532323','district','牟定县','156532300'),('156532324','district','南华县','156532300'),('156532325','district','姚安县','156532300'),('156532326','district','大姚县','156532300'),('156532327','district','永仁县','156532300'),('156532328','district','元谋县','156532300'),('156532329','district','武定县','156532300'),('156532331','district','禄丰县','156532300'),('156532500','city','红河哈尼族彝族自治州','156530000'),('156532501','district','个旧市','156532500'),('156532502','district','开远市','156532500'),('156532503','district','蒙自市','156532500'),('156532504','district','弥勒市','156532500'),('156532523','district','屏边苗族自治县','156532500'),('156532524','district','建水县','156532500'),('156532525','district','石屏县','156532500'),('156532527','district','泸西县','156532500'),('156532528','district','元阳县','156532500'),('156532529','district','红河县','156532500'),('156532530','district','金平苗族瑶族傣族自治县','156532500'),('156532531','district','绿春县','156532500'),('156532532','district','河口瑶族自治县','156532500'),('156532600','city','文山壮族苗族自治州','156530000'),('156532601','district','文山市','156532600'),('156532622','district','砚山县','156532600'),('156532623','district','西畴县','156532600'),('156532624','district','麻栗坡县','156532600'),('156532625','district',' 马关县','156532600'),('156532626','district','丘北县','156532600'),('156532627','district','广南县','156532600'),('156532628','district','富宁县','156532600'),('156532800','city','西双版纳傣族自治州','156530000'),('156532801','district','景洪市','156532800'),('156532822','district','勐海县','156532800'),('156532823','district','勐腊县','156532800'),('156532900','city','大理白族自治州','156530000'),('156532901','district','大理市','156532900'),('156532922','district','漾濞彝族自治县','156532900'),('156532923','district','祥云县','156532900'),('156532924','district','宾川县','156532900'),('156532925','district','弥渡县','156532900'),('156532926','district','南涧彝族自治县','156532900'),('156532927','district','巍山彝族回族自治县','156532900'),('156532928','district','永平县','156532900'),('156532929','district','云龙县','156532900'),('156532930','district','洱源县','156532900'),('156532931','district','剑川县','156532900'),('156532932','district','鹤庆县','156532900'),('156533100','city','德宏傣族景颇族自治州','156530000'),('156533102','district','瑞丽市','156533100'),('156533103','district','芒市','156533100'),('156533122','district','梁河县','156533100'),('156533123','district','盈江县','156533100'),('156533124','district','陇川县','156533100'),('156533300','city','怒江傈僳族自治州','156530000'),('156533301','district','泸水市','156533300'),('156533323','district','福贡县','156533300'),('156533324','district','贡山独龙族怒族自治县','156533300'),('156533325','district','兰坪白族普米族自治县','156533300'),('156533400','city','迪庆藏族自治州','156530000'),('156533401','district','香格里拉市','156533400'),('156533422','district','德钦县','156533400'),('156533423','district','维西傈僳族自治县','156533400'),('156540000','province','西藏自治区','156'),('156540100','city','拉萨市','156540000'),('156540102','district','城关区','156540100'),('156540103','district','堆龙德庆区','156540100'),('156540104','district','达孜区','156540100'),('156540121','district','林周县','156540100'),('156540122','district','当雄县','156540100'),('156540123','district','尼木县','156540100'),('156540124','district','曲水县','156540100'),('156540127','district','墨竹工卡县','156540100'),('156540171','district','格尔木藏青工业园区','156540100'),('156540172','district','拉萨经济技术开发区','156540100'),('156540173','district','西藏文化旅游创意园区','156540100'),('156540174','district','达孜工业园区','156540100'),('156540200','city','日喀则市','156540000'),('156540202','district','桑珠孜区','156540200'),('156540221','district','南 木林县','156540200'),('156540222','district','江孜县','156540200'),('156540223','district','定日县','156540200'),('156540224','district','萨迦县','156540200'),('156540225','district','拉孜县','156540200'),('156540226','district','昂仁县','156540200'),('156540227','district','谢通门县','156540200'),('156540228','district','白朗县','156540200'),('156540229','district','仁布县','156540200'),('156540230','district','康马县','156540200'),('156540231','district','定结县','156540200'),('156540232','district','仲巴县','156540200'),('156540233','district','亚东县','156540200'),('156540234','district','吉隆县','156540200'),('156540235','district','聂拉木县','156540200'),('156540236','district','萨嘎县','156540200'),('156540237','district','岗巴县','156540200'),('156540300','city','昌都市','156540000'),('156540302','district','卡若区','156540300'),('156540321','district','江达县','156540300'),('156540322','district','贡觉县','156540300'),('156540323','district','类乌齐县','156540300'),('156540324','district','丁青县','156540300'),('156540325','district','察雅县','156540300'),('156540326','district','八宿县','156540300'),('156540327','district','左贡县','156540300'),('156540328','district','芒康县','156540300'),('156540329','district','洛隆县','156540300'),('156540330','district','边坝县','156540300'),('156540400','city','林芝市','156540000'),('156540402','district','巴宜区','156540400'),('156540421','district','工布江达县','156540400'),('156540422','district','米林县','156540400'),('156540423','district','墨脱县','156540400'),('156540424','district','波密县','156540400'),('156540425','district','察隅县','156540400'),('156540426','district','朗县','156540400'),('156540500','city','山南市','156540000'),('156540502','district','乃东区','156540500'),('156540521','district','扎囊县','156540500'),('156540522','district','贡嘎县','156540500'),('156540523','district','桑日县','156540500'),('156540524','district','琼结县','156540500'),('156540525','district','曲松县','156540500'),('156540526','district','措美县','156540500'),('156540527','district','洛扎县','156540500'),('156540528','district','加查县','156540500'),('156540529','district','隆子县','156540500'),('156540530','district','错那县','156540500'),('156540531','district','浪卡子县','156540500'),('156540600','city','那曲市','156540000'),('156540602','district','色尼区','156540600'),('156540621','district','嘉黎县','156540600'),('156540622','district','比如县','156540600'),('156540623','district','聂荣县','156540600'),('156540624','district','安多县','156540600'),('156540625','district','申扎县','156540600'),('156540626','district','索县','156540600'),('156540627','district','班戈县','156540600'),('156540628','district','巴青县','156540600'),('156540629','district','尼玛县','156540600'),('156540630','district','双湖县','156540600'),('156542500','city','阿里地区','156540000'),('156542521','district','普兰县','156542500'),('156542522','district','札达县','156542500'),('156542523','district','噶尔县','156542500'),('156542524','district','日土县','156542500'),('156542525','district','革吉县','156542500'),('156542526','district','改则县','156542500'),('156542527','district','措勤县','156542500'),('156610000','province','陕西省','156'),('156610100','city','西安市','156610000'),('156610102','district','新城区','156610100'),('156610103','district','碑林区','156610100'),('156610104','district','莲湖区','156610100'),('156610111','district','灞桥区','156610100'),('156610112','district','未央区','156610100'),('156610113','district','雁塔区','156610100'),('156610114','district','阎良区','156610100'),('156610115','district','临潼区','156610100'),('156610116','district','长安区','156610100'),('156610117','district','高陵区','156610100'),('156610118','district','鄠邑区','156610100'),('156610122','district','蓝田县','156610100'),('156610124','district','周至县','156610100'),('156610200','city','铜川市','156610000'),('156610202','district','王益区','156610200'),('156610203','district','印台区','156610200'),('156610204','district','耀州区','156610200'),('156610222','district','宜君县','156610200'),('156610300','city','宝鸡市','156610000'),('156610302','district','渭滨区','156610300'),('156610303','district','金台区','156610300'),('156610304','district','陈仓区','156610300'),('156610322','district','凤翔县','156610300'),('156610323','district','岐山县','156610300'),('156610324','district','扶风县','156610300'),('156610326','district','眉县','156610300'),('156610327','district','陇县','156610300'),('156610328','district','千阳县','156610300'),('156610329','district','麟游县','156610300'),('156610330','district','凤县','156610300'),('156610331','district','太白县','156610300'),('156610400','city','咸阳市','156610000'),('156610402','district','秦都区','156610400'),('156610403','district','杨陵区','156610400'),('156610404','district','渭城区','156610400'),('156610422','district','三原县','156610400'),('156610423','district','泾阳县','156610400'),('156610424','district','乾县','156610400'),('156610425','district','礼泉县','156610400'),('156610426','district','永寿县','156610400'),('156610428','district','长武县','156610400'),('156610429','district','旬邑县','156610400'),('156610430','district','淳化县','156610400'),('156610431','district','武功县','156610400'),('156610481','district','兴平市','156610400'),('156610482','district','彬州市','156610400'),('156610500','city','渭南市','156610000'),('156610502','district','临渭区','156610500'),('156610503','district','华州区','156610500'),('156610522','district','潼关县','156610500'),('156610523','district','大荔县','156610500'),('156610524','district','合阳县','156610500'),('156610525','district','澄城县','156610500'),('156610526','district','蒲城县','156610500'),('156610527','district','白水县','156610500'),('156610528','district','富平县','156610500'),('156610581','district','韩城市','156610500'),('156610582','district','华阴市','156610500'),('156610600','city','延安市','156610000'),('156610602','district','宝塔区','156610600'),('156610603','district','安塞区','156610600'),('156610621','district','延长县','156610600'),('156610622','district','延川县','156610600'),('156610625','district','志丹县','156610600'),('156610626','district','吴起县','156610600'),('156610627','district','甘泉县','156610600'),('156610628','district','富县','156610600'),('156610629','district','洛川县','156610600'),('156610630','district','宜川县','156610600'),('156610631','district','黄龙县','156610600'),('156610632','district','黄陵县','156610600'),('156610681','district','子长市','156610600'),('156610700','city','汉中市','156610000'),('156610702','district','汉台区','156610700'),('156610703','district','南郑区','156610700'),('156610722','district','城固县','156610700'),('156610723','district','洋县','156610700'),('156610724','district','西乡县','156610700'),('156610725','district','勉县','156610700'),('156610726','district','宁强县','156610700'),('156610727','district','略阳县','156610700'),('156610728','district','镇巴县','156610700'),('156610729','district','留坝县','156610700'),('156610730','district','佛坪县','156610700'),('156610800','city','榆林市','156610000'),('156610802','district','榆阳区','156610800'),('156610803','district','横山区','156610800'),('156610822','district','府谷县','156610800'),('156610824','district','靖边县','156610800'),('156610825','district','定边县','156610800'),('156610826','district','绥德县','156610800'),('156610827','district','米脂县','156610800'),('156610828','district','佳县','156610800'),('156610829','district','吴堡县','156610800'),('156610830','district','清涧县','156610800'),('156610831','district','子洲县','156610800'),('156610881','district','神木市','156610800'),('156610900','city','安康市','156610000'),('156610902','district','汉滨区','156610900'),('156610921','district','汉阴县','156610900'),('156610922','district','石泉县','156610900'),('156610923','district','宁陕县','156610900'),('156610924','district','紫阳县','156610900'),('156610925','district','岚皋县','156610900'),('156610926','district','平利县','156610900'),('156610927','district','镇坪县','156610900'),('156610928','district','旬阳县','156610900'),('156610929','district',' 白河县','156610900'),('156611000','city','商洛市','156610000'),('156611002','district','商州区','156611000'),('156611021','district','洛南县','156611000'),('156611022','district','丹凤县','156611000'),('156611023','district','商南县','156611000'),('156611024','district','山阳县','156611000'),('156611025','district','镇安县','156611000'),('156611026','district','柞水县','156611000'),('156620000','province','甘肃省','156'),('156620100','city','兰州市','156620000'),('156620102','district','城关区','156620100'),('156620103','district','七里河区','156620100'),('156620104','district','西固 区','156620100'),('156620105','district','安宁区','156620100'),('156620111','district','红古区','156620100'),('156620121','district','永登县','156620100'),('156620122','district','皋兰县','156620100'),('156620123','district','榆中县','156620100'),('156620171','district','兰州新区','156620100'),('156620200','city','嘉峪关市','156620000'),('156620201','district','嘉峪关市','156620200'),('156620300','city','金昌市','156620000'),('156620302','district','金川区','156620300'),('156620321','district','永昌县','156620300'),('156620400','city','白银市','156620000'),('156620402','district','白银区','156620400'),('156620403','district','平川区','156620400'),('156620421','district','靖远县','156620400'),('156620422','district','会宁县','156620400'),('156620423','district','景泰县','156620400'),('156620500','city','天水市','156620000'),('156620502','district','秦州区','156620500'),('156620503','district','麦积区','156620500'),('156620521','district','清水县','156620500'),('156620522','district','秦安县','156620500'),('156620523','district','甘谷县','156620500'),('156620524','district','武山县','156620500'),('156620525','district','张家川回族自治县','156620500'),('156620600','city','武威市','156620000'),('156620602','district','凉州区','156620600'),('156620621','district','民勤县','156620600'),('156620622','district','古浪县','156620600'),('156620623','district','天祝藏族自治县','156620600'),('156620700','city','张掖市','156620000'),('156620702','district','甘州区','156620700'),('156620721','district','肃南裕固族自治县','156620700'),('156620722','district','民乐县','156620700'),('156620723','district','临泽县','156620700'),('156620724','district','高台县','156620700'),('156620725','district','山丹县','156620700'),('156620800','city','平凉市','156620000'),('156620802','district','崆峒区','156620800'),('156620821','district','泾川县','156620800'),('156620822','district','灵台县','156620800'),('156620823','district','崇信县','156620800'),('156620825','district','庄浪县','156620800'),('156620826','district','静宁县','156620800'),('156620881','district','华亭市','156620800'),('156620900','city','酒泉市','156620000'),('156620902','district','肃州区','156620900'),('156620921','district','金塔县','156620900'),('156620922','district','瓜州县','156620900'),('156620923','district','肃北蒙古族自治县','156620900'),('156620924','district','阿克塞哈萨克族自治县','156620900'),('156620981','district','玉 门市','156620900'),('156620982','district','敦煌市','156620900'),('156621000','city','庆阳市','156620000'),('156621002','district',' 西峰区','156621000'),('156621021','district','庆城县','156621000'),('156621022','district','环县','156621000'),('156621023','district','华池县','156621000'),('156621024','district','合水县','156621000'),('156621025','district','正宁县','156621000'),('156621026','district','宁县','156621000'),('156621027','district','镇原县','156621000'),('156621100','city','定西市','156620000'),('156621102','district','安定区','156621100'),('156621121','district','通渭县','156621100'),('156621122','district','陇西县','156621100'),('156621123','district','渭源县','156621100'),('156621124','district','临洮县','156621100'),('156621125','district','漳县','156621100'),('156621126','district','岷县','156621100'),('156621200','city','陇南市','156620000'),('156621202','district','武都区','156621200'),('156621221','district','成县','156621200'),('156621222','district','文县','156621200'),('156621223','district','宕昌县','156621200'),('156621224','district','康县','156621200'),('156621225','district','西和县','156621200'),('156621226','district','礼县','156621200'),('156621227','district','徽县','156621200'),('156621228','district','两当县','156621200'),('156622900','city','临夏回族自治州','156620000'),('156622901','district','临夏市','156622900'),('156622921','district','临夏县','156622900'),('156622922','district','康乐县','156622900'),('156622923','district','永靖县','156622900'),('156622924','district','广河县','156622900'),('156622925','district','和政县','156622900'),('156622926','district','东乡族自治县','156622900'),('156622927','district','积石山保安族东乡族撒拉族自治县','156622900'),('156623000','city','甘南藏族自治州','156620000'),('156623001','district','合作市','156623000'),('156623021','district','临潭县','156623000'),('156623022','district','卓尼县','156623000'),('156623023','district','舟曲县','156623000'),('156623024','district','迭部县','156623000'),('156623025','district','玛曲县','156623000'),('156623026','district','碌曲县','156623000'),('156623027','district','夏河县','156623000'),('156630000','province','青海省','156'),('156630100','city','西宁市','156630000'),('156630102','district','城东区','156630100'),('156630103','district','城中区','156630100'),('156630104','district','城西区','156630100'),('156630105','district','城北区','156630100'),('156630106','district','湟中区','156630100'),('156630121','district','大通回族土族自治县','156630100'),('156630123','district','湟源县','156630100'),('156630200','city','海东市','156630000'),('156630202','district','乐都区','156630200'),('156630203','district','平安区','156630200'),('156630222','district','民和回族土族自治县','156630200'),('156630223','district','互助土族自治县','156630200'),('156630224','district','化隆回族自治县','156630200'),('156630225','district','循化撒拉族自治县','156630200'),('156632200','city','海北藏族自治州','156630000'),('156632221','district','门源回族自治县','156632200'),('156632222','district','祁连县','156632200'),('156632223','district','海晏县','156632200'),('156632224','district','刚察县','156632200'),('156632300','city','黄南藏族自治州','156630000'),('156632321','district','同仁县','156632300'),('156632322','district','尖扎县','156632300'),('156632323','district','泽库县','156632300'),('156632324','district','河南蒙古族自治县','156632300'),('156632500','city','海南藏族自治州','156630000'),('156632521','district','共和县','156632500'),('156632522','district','同德县','156632500'),('156632523','district','贵德县','156632500'),('156632524','district','兴海县','156632500'),('156632525','district','贵南县','156632500'),('156632600','city','果洛藏族自治州','156630000'),('156632621','district','玛沁县','156632600'),('156632622','district','班玛县','156632600'),('156632623','district','甘德县','156632600'),('156632624','district','达日县','156632600'),('156632625','district','久治县','156632600'),('156632626','district','玛多县','156632600'),('156632700','city','玉树藏族自治州','156630000'),('156632701','district','玉树市','156632700'),('156632722','district','杂多县','156632700'),('156632723','district','称多县','156632700'),('156632724','district','治多县','156632700'),('156632725','district','囊谦县','156632700'),('156632726','district','曲麻莱县','156632700'),('156632800','city','海西蒙古族藏族自治州','156630000'),('156632801','district','格尔木市','156632800'),('156632802','district','德令哈市','156632800'),('156632803','district','茫崖市','156632800'),('156632821','district','乌兰县','156632800'),('156632822','district','都兰县','156632800'),('156632823','district','天峻县','156632800'),('156632857','district','大柴旦行政委员会','156632800'),('156640000','province','宁夏回族自治区','156'),('156640100','city','银川市','156640000'),('156640104','district','兴庆区','156640100'),('156640105','district','西夏区','156640100'),('156640106','district','金凤区','156640100'),('156640121','district','永宁县','156640100'),('156640122','district','贺兰县','156640100'),('156640181','district','灵武市','156640100'),('156640200','city','石嘴山市','156640000'),('156640202','district','大武口区','156640200'),('156640205','district','惠农区','156640200'),('156640221','district','平罗县','156640200'),('156640300','city','吴忠市','156640000'),('156640302','district','利通区','156640300'),('156640303','district','红寺堡区','156640300'),('156640323','district','盐池县','156640300'),('156640324','district','同心县','156640300'),('156640381','district','青铜峡市','156640300'),('156640400','city','固原市','156640000'),('156640402','district','原州 区','156640400'),('156640422','district','西吉县','156640400'),('156640423','district','隆德县','156640400'),('156640424','district','泾源县','156640400'),('156640425','district','彭阳县','156640400'),('156640500','city','中卫市','156640000'),('156640502','district','沙坡头区','156640500'),('156640521','district','中宁县','156640500'),('156640522','district','海原县','156640500'),('156650000','province','新疆维吾尔自治区','156'),('156650100','city','乌鲁木齐市','156650000'),('156650102','district','天山区','156650100'),('156650103','district','沙依巴克区','156650100'),('156650104','district','新市区','156650100'),('156650105','district','水磨沟区','156650100'),('156650106','district','头屯河区','156650100'),('156650107','district','达坂城区','156650100'),('156650109','district','米东区','156650100'),('156650121','district','乌鲁木齐县','156650100'),('156650200','city','克拉玛依市','156650000'),('156650202','district','独山子区','156650200'),('156650203','district','克拉玛依区','156650200'),('156650204','district','白碱滩区','156650200'),('156650205','district','乌尔禾区','156650200'),('156650400','city','吐鲁番市','156650000'),('156650402','district','高昌区','156650400'),('156650421','district','鄯善县','156650400'),('156650422','district','托克逊县','156650400'),('156650500','city','哈密市','156650000'),('156650502','district','伊州区','156650500'),('156650521','district','巴里坤哈萨克自治县','156650500'),('156650522','district','伊吾县','156650500'),('156652300','city','昌吉回族自治州','156650000'),('156652301','district','昌吉市','156652300'),('156652302','district','阜康市','156652300'),('156652323','district','呼图壁县','156652300'),('156652324','district','玛纳斯县','156652300'),('156652325','district','奇台县','156652300'),('156652327','district','吉木萨尔县','156652300'),('156652328','district','木垒哈萨克自治县','156652300'),('156652700','city','博尔塔拉蒙古自治州','156650000'),('156652701','district','博乐市','156652700'),('156652702','district','阿拉山口市','156652700'),('156652722','district','精河县','156652700'),('156652723','district','温泉县','156652700'),('156652800','city','巴音郭楞蒙古自治州','156650000'),('156652801','district','库尔勒市','156652800'),('156652822','district','轮台县','156652800'),('156652823','district','尉犁县','156652800'),('156652824','district','若羌县','156652800'),('156652825','district','且末县','156652800'),('156652826','district','焉耆回族自治县','156652800'),('156652827','district','和静县','156652800'),('156652828','district','和硕县','156652800'),('156652829','district','博湖县','156652800'),('156652871','district','库尔勒经济技术开发区','156652800'),('156652900','city','阿克苏地区','156650000'),('156652901','district','阿 克苏市','156652900'),('156652902','district','库车市','156652900'),('156652922','district','温宿县','156652900'),('156652924','district','沙雅县','156652900'),('156652925','district','新和县','156652900'),('156652926','district','拜城县','156652900'),('156652927','district','乌什县','156652900'),('156652928','district','阿瓦提县','156652900'),('156652929','district','柯坪县','156652900'),('156653000','city','克孜勒苏柯尔克孜自治州','156650000'),('156653001','district','阿图什市','156653000'),('156653022','district','阿克陶县','156653000'),('156653023','district','阿合奇县','156653000'),('156653024','district','乌恰县','156653000'),('156653100','city','喀什地区','156650000'),('156653101','district','喀什市','156653100'),('156653121','district','疏附县','156653100'),('156653122','district','疏勒县','156653100'),('156653123','district','英吉沙县','156653100'),('156653124','district','泽普县','156653100'),('156653125','district','莎车县','156653100'),('156653126','district','叶城县','156653100'),('156653127','district','麦盖提县','156653100'),('156653128','district','岳普湖县','156653100'),('156653129','district','伽师县','156653100'),('156653130','district','巴楚县','156653100'),('156653131','district','塔什库尔干塔吉克自治县','156653100'),('156653200','city','和田地区','156650000'),('156653201','district','和田市','156653200'),('156653221','district','和田县','156653200'),('156653222','district','墨玉县','156653200'),('156653223','district','皮山县','156653200'),('156653224','district','洛浦县','156653200'),('156653225','district','策勒县','156653200'),('156653226','district','于田县','156653200'),('156653227','district','民丰县','156653200'),('156654000','city','伊犁哈萨克自治州','156650000'),('156654002','district','伊宁市','156654000'),('156654003','district',' 奎屯市','156654000'),('156654004','district','霍尔果斯市','156654000'),('156654021','district','伊宁县','156654000'),('156654022','district','察布查尔锡伯自治县','156654000'),('156654023','district','霍城县','156654000'),('156654024','district','巩留县','156654000'),('156654025','district','新源县','156654000'),('156654026','district','昭苏县','156654000'),('156654027','district','特克斯县','156654000'),('156654028','district','尼勒克县','156654000'),('156654200','city','塔城地区','156650000'),('156654201','district','塔城市','156654200'),('156654202','district','乌苏市','156654200'),('156654221','district','额敏县','156654200'),('156654223','district','沙湾县','156654200'),('156654224','district','托里县','156654200'),('156654225','district','裕民县','156654200'),('156654226','district','和布克赛尔蒙古自治县','156654200'),('156654300','city','阿勒泰地区','156650000'),('156654301','district','阿勒泰市','156654300'),('156654321','district','布尔津县','156654300'),('156654322','district','富蕴县','156654300'),('156654323','district','福海县','156654300'),('156654324','district','哈巴河县','156654300'),('156654325','district','青河县','156654300'),('156654326','district','吉木乃县','156654300'),('156659000','city','自治区直辖县级行政区划','156650000'),('156659001','district','石河子市','156659000'),('156659002','district','阿拉尔市','156659000'),('156659003','district','图木舒克市','156659000'),('156659004','district','五家渠市','156659000'),('156659005','district','北屯市','156659000'),('156659006','district','铁门关市','156659000'),('156659007','district','双河市','156659000'),('156659008','district','可克达拉市','156659000'),('156659009','district','昆玉市','156659000'),('156659010','district','胡杨河市','156659000'),('156710000','province','台湾省','156'),('156810000','province','香港特别行政区','156'),('156810001','district','中西区','156810100'),('156810002','district','湾仔区','156810100'),('156810003','district','东区','156810100'),('156810004','district','南区','156810100'),('156810005','district','油尖旺区','156810100'),('156810006','district','深水埗区','156810100'),('156810007','district','九龙城区','156810100'),('156810008','district',' 黄大仙区','156810100'),('156810009','district','观塘区','156810100'),('156810010','district','荃湾区','156810100'),('156810011','district','屯门区','156810100'),('156810012','district','元朗区','156810100'),('156810013','district','北区','156810100'),('156810014','district','大埔区','156810100'),('156810015','district','西贡区','156810100'),('156810016','district','沙田区','156810100'),('156810017','district','葵青区','156810100'),('156810018','district','离岛区','156810100'),('156810100','city','香港特别行政区','156810000'),('156820000','province','澳门特别行政区','156'),('156820100','city','澳门特别行政区','156820000'),('156820101','district','澳门特别行政区','156820100');
/*!40000 ALTER TABLE `area` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_api_traffic` (
  `id` bigint NOT NULL COMMENT 'ID',
  `api` varchar(255) NOT NULL COMMENT 'api',
  `threshold` int NOT NULL DEFAULT '2' COMMENT '阈值',
  `alive` int NOT NULL DEFAULT '0' COMMENT '活动并发',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='API并发限流阈值设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_api_traffic` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_api_traffic` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_area_custom` (
  `id` varchar(255) NOT NULL COMMENT '主键',
  `name` varchar(255) NOT NULL COMMENT '自定义区域名称',
  `pid` varchar(255) NOT NULL COMMENT '父级ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自定义地图区域信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_area_custom` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_area_custom` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_chart_view` (
  `id` bigint NOT NULL COMMENT 'ID',
  `title` varchar(1024) DEFAULT NULL COMMENT '标题',
  `scene_id` bigint NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id',
  `table_id` bigint DEFAULT NULL COMMENT '数据集表ID',
  `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
  `render` varchar(50) DEFAULT NULL COMMENT '图表渲染方式',
  `result_count` int DEFAULT NULL COMMENT '展示结果',
  `result_mode` varchar(50) DEFAULT NULL COMMENT '展示模式',
  `x_axis` longtext COMMENT '横轴field',
  `x_axis_ext` longtext COMMENT 'table-row',
  `y_axis` longtext COMMENT '纵轴field',
  `y_axis_ext` longtext COMMENT '副轴',
  `ext_stack` longtext COMMENT '堆叠项',
  `ext_bubble` longtext COMMENT '气泡大小',
  `ext_label` longtext COMMENT '动态标签',
  `ext_tooltip` longtext COMMENT '动态提示',
  `custom_attr` longtext COMMENT '图形属性',
  `custom_attr_mobile` longtext COMMENT '图形属性_移动端',
  `custom_style` longtext COMMENT '组件样式',
  `custom_style_mobile` longtext COMMENT '组件样式_移动端',
  `custom_filter` longtext COMMENT '结果过滤',
  `drill_fields` longtext COMMENT '钻取字段',
  `senior` longtext COMMENT '高级',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `snapshot` longtext COMMENT '缩略图 ',
  `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 图表',
  `chart_type` varchar(255) DEFAULT 'private' COMMENT '图表类型 public 公共 历史可复用的图表，private 私有 专属某个仪表板',
  `is_plugin` bit(1) DEFAULT NULL COMMENT '是否插件',
  `data_from` varchar(255) DEFAULT 'dataset' COMMENT '数据来源 template 模板数据 dataset 数据集数据',
  `view_fields` longtext COMMENT '图表字段集合',
  `refresh_view_enable` tinyint DEFAULT '0' COMMENT '是否开启刷新',
  `refresh_unit` varchar(255) DEFAULT 'minute' COMMENT '刷新时间单位',
  `refresh_time` int DEFAULT '5' COMMENT '刷新时间',
  `linkage_active` tinyint DEFAULT '0' COMMENT '是否开启联动',
  `jump_active` tinyint DEFAULT '0' COMMENT '是否开启跳转',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制ID',
  `aggregate` bit(1) DEFAULT NULL COMMENT '区间条形图开启时间纬度开启聚合',
  `flow_map_start_name` longtext COMMENT '流向地图起点名称field',
  `flow_map_end_name` longtext COMMENT '流向地图终点名称field',
  `ext_color` longtext COMMENT '颜色维度field',
  `sort_priority` longtext COMMENT '字段排序优先级',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组件图表表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_chart_view` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_chart_view` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_copilot_config` (
  `id` bigint NOT NULL COMMENT 'ID',
  `copilot_url` varchar(255) DEFAULT NULL COMMENT 'copilot服务端地址',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  `pwd` varchar(255) DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='copilot配置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_copilot_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_copilot_config` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_copilot_msg` (
  `id` bigint NOT NULL COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `dataset_group_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `msg_type` varchar(255) DEFAULT NULL COMMENT 'user or api',
  `engine_type` varchar(255) DEFAULT NULL COMMENT 'mysql oracle ...',
  `schema_sql` longtext COMMENT 'create sql',
  `question` longtext COMMENT '用户提问',
  `history` longtext COMMENT '历史信息',
  `copilot_sql` longtext COMMENT 'copilot 返回 sql',
  `api_msg` longtext COMMENT 'copilot 返回信息',
  `sql_ok` int DEFAULT NULL COMMENT 'sql 状态',
  `chart_ok` int DEFAULT NULL COMMENT 'chart 状态',
  `chart` longtext COMMENT 'chart 内容',
  `chart_data` longtext COMMENT '视图数据',
  `exec_sql` longtext COMMENT '执行请求的SQL',
  `msg_status` int DEFAULT NULL COMMENT 'msg状态，0失败 1成功',
  `err_msg` longtext COMMENT 'de错误信息',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='copilot问答信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_copilot_msg` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_copilot_msg` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_copilot_token` (
  `id` bigint NOT NULL COMMENT 'ID',
  `type` varchar(255) DEFAULT NULL COMMENT 'free or license',
  `token` longtext COMMENT 'token值',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='copilot token记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_copilot_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_copilot_token` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_custom_geo_area` (
  `id` varchar(50) NOT NULL COMMENT 'id',
  `name` varchar(50) DEFAULT NULL COMMENT '区域名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自定义地理区域';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_custom_geo_area` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_custom_geo_area` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_custom_geo_sub_area` (
  `id` bigint NOT NULL COMMENT 'id',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `scope` varchar(1024) DEFAULT NULL COMMENT '区域范围',
  `geo_area_id` varchar(50) NOT NULL COMMENT '自定义地理区域id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自定义地理区域分区详情';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_custom_geo_sub_area` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_custom_geo_sub_area` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_dataset_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(128) DEFAULT NULL COMMENT '名称',
  `pid` bigint DEFAULT NULL COMMENT '父级ID',
  `level` int DEFAULT '0' COMMENT '当前分组处于第几级',
  `node_type` varchar(50) NOT NULL COMMENT 'node类型：folder or dataset',
  `type` varchar(50) DEFAULT NULL COMMENT 'sql,union',
  `mode` int DEFAULT '0' COMMENT '连接模式：0-直连，1-同步(包括excel、api等数据存在de中的表)',
  `info` longtext COMMENT '关联关系树',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `qrtz_instance` varchar(1024) DEFAULT NULL COMMENT 'Quartz 实例 ID',
  `sync_status` varchar(45) DEFAULT NULL COMMENT '同步状态',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人ID',
  `last_update_time` bigint DEFAULT '0' COMMENT '最后同步时间',
  `union_sql` longtext COMMENT '关联sql',
  `is_cross` bit(1) DEFAULT NULL COMMENT '是否跨源',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据集分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_dataset_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_dataset_group` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_dataset_sync_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataset_group_id` bigint NOT NULL COMMENT '数据集ID',
  `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `update_type` varchar(50) DEFAULT 'all_scope' COMMENT '更新方式：all_scope 全量，add_scope 增量',
  `incremental_field_id` bigint DEFAULT NULL COMMENT '增量字段ID',
  `incremental_last_value` varchar(255) DEFAULT NULL COMMENT '增量字段上次同步值',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间',
  `sync_rate` varchar(50) DEFAULT 'RIGHTNOW' COMMENT '执行频率',
  `cron` varchar(255) DEFAULT NULL COMMENT 'cron表达式',
  `simple_cron_value` bigint DEFAULT NULL COMMENT '简单重复间隔',
  `simple_cron_type` varchar(50) DEFAULT NULL COMMENT '简单重复类型：分、时、天',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `last_exec_time` bigint DEFAULT NULL COMMENT '上次执行时间',
  `last_exec_status` varchar(50) DEFAULT NULL COMMENT '上次执行结果',
  `task_status` varchar(50) DEFAULT 'WaitingForExecution' COMMENT '任务状态',
  `cache_ready` tinyint DEFAULT '0' COMMENT '缓存表是否已可用',
  `heartbeat_time` bigint DEFAULT NULL COMMENT '运行中心跳时间',
  `schema_hash` varchar(128) DEFAULT NULL COMMENT '数据集同步字段结构签名',
  `full_sync_interval_hours` int DEFAULT '24' COMMENT '增量任务周期性全量校准间隔小时，0表示关闭',
  `last_full_sync_time` bigint DEFAULT NULL COMMENT '最近一次全量同步时间',
  `verify_enabled` tinyint DEFAULT '1' COMMENT '同步后是否执行源端与缓存对账',
  `last_verify_time` bigint DEFAULT NULL COMMENT '最近一次对账时间',
  `last_verify_status` varchar(50) DEFAULT NULL COMMENT '最近一次对账状态',
  `last_verify_message` text COMMENT '最近一次对账信息',
  `last_source_row_count` bigint DEFAULT NULL COMMENT '最近一次源端行数',
  `last_cache_row_count` bigint DEFAULT NULL COMMENT '最近一次缓存行数',
  `cache_expire_hours` int DEFAULT '26' COMMENT '缓存过期提示阈值小时，0表示关闭',
  `task_timeout_minutes` int DEFAULT '360' COMMENT '单次同步任务超时分钟，0表示关闭',
  `consecutive_failures` int DEFAULT '0' COMMENT '连续失败次数',
  `failure_warn_threshold` int DEFAULT '1' COMMENT '连续失败告警阈值，0表示关闭',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dataset_sync_task_dataset` (`dataset_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据集同步任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_dataset_sync_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_dataset_sync_task` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_dataset_sync_task_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataset_group_id` bigint NOT NULL COMMENT '数据集ID',
  `task_id` bigint DEFAULT NULL COMMENT '任务ID',
  `update_type` varchar(50) DEFAULT NULL COMMENT '更新方式',
  `table_name` varchar(255) DEFAULT NULL COMMENT '引擎表名',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `task_status` varchar(50) DEFAULT NULL COMMENT '执行状态',
  `row_count` bigint DEFAULT '0' COMMENT '同步行数',
  `info` text COMMENT '执行信息',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `trigger_type` varchar(50) DEFAULT NULL COMMENT '触发类型',
  PRIMARY KEY (`id`),
  KEY `idx_dataset_sync_task_log_A` (`dataset_group_id`,`task_id`,`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据集同步任务日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_dataset_sync_task_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_dataset_sync_task_log` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_dataset_table` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(128) DEFAULT NULL COMMENT '名称',
  `table_name` varchar(128) DEFAULT NULL COMMENT '物理表名',
  `datasource_id` bigint DEFAULT NULL COMMENT '数据源ID',
  `dataset_group_id` bigint NOT NULL COMMENT '数据集ID',
  `type` varchar(50) DEFAULT NULL COMMENT 'db,sql,union,excel,api',
  `info` longtext COMMENT '表原始信息,表名,sql等',
  `sql_variable_details` longtext COMMENT 'SQL参数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='table数据集';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_dataset_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_dataset_table` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_dataset_table_field` (
  `id` bigint NOT NULL COMMENT 'ID',
  `datasource_id` bigint DEFAULT NULL COMMENT '数据源ID',
  `dataset_table_id` bigint DEFAULT NULL COMMENT '数据表ID',
  `dataset_group_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `chart_id` bigint DEFAULT NULL COMMENT '图表ID',
  `origin_name` longtext NOT NULL COMMENT '原始字段名',
  `name` longtext COMMENT '字段名用于展示',
  `description` longtext COMMENT '描述',
  `dataease_name` varchar(255) DEFAULT NULL COMMENT '字段唯一标识',
  `field_short_name` varchar(255) DEFAULT NULL COMMENT 'de字段别名',
  `group_list` longtext COMMENT '分组设置',
  `other_group` longtext COMMENT '未分组的值',
  `group_type` varchar(50) DEFAULT NULL COMMENT '维度/指标标识 d:维度，q:指标',
  `type` varchar(255) NOT NULL COMMENT '原始字段类型',
  `size` int DEFAULT NULL COMMENT '字段长度（允许为空，默认0）',
  `de_type` int NOT NULL COMMENT 'Crest字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制，7-URL',
  `de_extract_type` int NOT NULL COMMENT 'de记录的原始类型',
  `ext_field` int DEFAULT NULL COMMENT '是否扩展字段 0原始 1复制 2计算字段...',
  `checked` tinyint DEFAULT '1' COMMENT '是否选中',
  `column_index` int DEFAULT NULL COMMENT '列位置',
  `last_sync_time` bigint DEFAULT NULL COMMENT '同步时间',
  `accuracy` int DEFAULT '0' COMMENT '精度',
  `date_format` varchar(255) DEFAULT NULL COMMENT '时间字段类型',
  `date_format_type` varchar(255) DEFAULT NULL COMMENT '时间格式类型',
  `params` text COMMENT '计算字段参数',
  `order_checked` tinyint DEFAULT '0' COMMENT '是否排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='table数据集表字段';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_dataset_table_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_dataset_table_field` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_dataset_table_sql_log` (
  `id` varchar(50) NOT NULL DEFAULT '' COMMENT 'ID',
  `table_id` varchar(50) NOT NULL DEFAULT '' COMMENT '数据集SQL节点ID',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `spend` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `sql` longtext NOT NULL COMMENT '详细信息',
  `status` varchar(45) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='table数据集查询sql日志';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_dataset_table_sql_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_dataset_table_sql_log` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_datasource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `type` varchar(50) NOT NULL COMMENT '类型',
  `pid` bigint DEFAULT NULL COMMENT '父级ID',
  `edit_type` varchar(50) DEFAULT NULL COMMENT '更新方式：0：替换；1：追加',
  `configuration` longtext NOT NULL COMMENT '详细信息',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '变更人',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `status` longtext COMMENT '状态',
  `qrtz_instance` longtext COMMENT '状态',
  `task_status` varchar(50) DEFAULT NULL COMMENT '任务状态',
  `enable_data_fill` tinyint DEFAULT '0' COMMENT '启用数据填报功能',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_datasource` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_datasource` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_datasource_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ds_id` bigint NOT NULL COMMENT '数据源ID',
  `name` varchar(255) NOT NULL COMMENT '任务名称',
  `update_type` varchar(50) NOT NULL COMMENT '更新方式',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间',
  `sync_rate` varchar(50) NOT NULL COMMENT '执行频率：0 一次性 1 cron',
  `cron` varchar(255) DEFAULT NULL COMMENT 'cron表达式',
  `simple_cron_value` bigint DEFAULT NULL COMMENT '简单重复间隔',
  `simple_cron_type` varchar(50) DEFAULT NULL COMMENT '简单重复类型：分、时、天',
  `end_limit` varchar(50) DEFAULT NULL COMMENT '结束限制 0 无限制 1 设定结束时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `last_exec_time` bigint DEFAULT NULL COMMENT '上次执行时间',
  `last_exec_status` varchar(50) DEFAULT NULL COMMENT '上次执行结果',
  `extra_data` longtext COMMENT '额外数据',
  `task_status` varchar(50) DEFAULT NULL COMMENT '任务状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源定时同步任务';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_datasource_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_datasource_task` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_datasource_task_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ds_id` bigint NOT NULL COMMENT '数据源ID',
  `task_id` bigint DEFAULT NULL COMMENT '任务ID',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `task_status` varchar(50) NOT NULL COMMENT '执行状态',
  `table_name` varchar(255) NOT NULL COMMENT '表名',
  `info` longtext COMMENT '错误信息',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `trigger_type` varchar(45) DEFAULT NULL COMMENT '更新频率类型',
  PRIMARY KEY (`id`),
  KEY `idx_dataset_table_task_log_ds_id` (`ds_id`),
  KEY `idx_dataset_table_task_log_task_id` (`task_id`),
  KEY `idx_dataset_table_task_log_A` (`ds_id`,`table_name`,`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源定时同步任务执行日志';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_datasource_task_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_datasource_task_log` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_de_engine` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `description` varchar(50) DEFAULT NULL COMMENT '描述',
  `type` varchar(50) NOT NULL COMMENT '类型',
  `configuration` longtext NOT NULL COMMENT '详细信息',
  `create_time` bigint DEFAULT NULL COMMENT 'Create timestamp',
  `update_time` bigint DEFAULT NULL COMMENT 'Update timestamp',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `status` varchar(45) DEFAULT NULL COMMENT '状态',
  `enable_data_fill` tinyint DEFAULT '1' COMMENT '启用数据填报功能',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据引擎';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_de_engine` DISABLE KEYS */;
INSERT INTO `core_de_engine` VALUES (1,'默认引擎','默认引擎','mysql','',NULL,NULL,NULL,NULL,1);
/*!40000 ALTER TABLE `core_de_engine` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_driver` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `type` varchar(255) DEFAULT NULL COMMENT '数据源类型',
  `driver_class` varchar(255) DEFAULT NULL COMMENT '驱动类',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='驱动';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_driver` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_driver` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_driver_jar` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `de_driver_id` varchar(50) NOT NULL COMMENT '驱动主键',
  `file_name` varchar(255) DEFAULT NULL COMMENT '名称',
  `version` varchar(255) DEFAULT NULL COMMENT '版本',
  `driver_class` longtext COMMENT '驱动类',
  `trans_name` varchar(255) DEFAULT NULL COMMENT '替换后的 jar 包名称',
  `is_trans_name` tinyint DEFAULT NULL COMMENT '是否将上传 jar 包替换了名称（1-是，0-否）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='驱动详情';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_driver_jar` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_driver_jar` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_ds_finish_page` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='是否显示完成页面记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_ds_finish_page` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_ds_finish_page` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_export_download_task` (
  `id` varchar(255) NOT NULL,
  `create_time` bigint DEFAULT NULL,
  `valid_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='下载任务列表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_export_download_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_export_download_task` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_export_task` (
  `id` varchar(255) NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `file_name` varchar(2048) DEFAULT NULL COMMENT '文件名称',
  `file_size` double DEFAULT NULL COMMENT '文件大小',
  `file_size_unit` varchar(255) DEFAULT NULL COMMENT '单位',
  `export_from` varchar(255) DEFAULT NULL COMMENT '导出来源ID',
  `export_status` varchar(255) DEFAULT NULL COMMENT '导出状态',
  `export_from_type` varchar(255) DEFAULT NULL COMMENT '导出来源类型',
  `export_time` bigint DEFAULT NULL COMMENT '导出时间',
  `export_progress` varchar(255) DEFAULT NULL COMMENT '导出进度',
  `export_machine_name` varchar(512) DEFAULT NULL COMMENT '导出机器名称',
  `params` longtext NOT NULL COMMENT '过滤参数',
  `msg` longtext COMMENT '错误信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导出任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_export_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_export_task` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_font` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '字体名称',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
  `file_trans_name` varchar(255) DEFAULT NULL COMMENT '文件转换名称',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  `is_BuiltIn` tinyint DEFAULT '0' COMMENT '是否内置',
  `size` double DEFAULT NULL COMMENT '文件大小',
  `size_type` varchar(255) DEFAULT NULL COMMENT '存储单位',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字体表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_font` DISABLE KEYS */;
INSERT INTO `core_font` VALUES (1,'PingFang',NULL,NULL,1,0,1,NULL,NULL);
/*!40000 ALTER TABLE `core_font` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` bigint unsigned NOT NULL COMMENT '父ID',
  `type` int DEFAULT NULL COMMENT '类型',
  `name` varchar(45) DEFAULT NULL COMMENT '名称',
  `component` varchar(45) DEFAULT NULL COMMENT '组件',
  `menu_sort` int DEFAULT NULL COMMENT '排序',
  `icon` varchar(45) DEFAULT NULL COMMENT '图标',
  `path` varchar(45) DEFAULT NULL COMMENT '路径',
  `hidden` tinyint NOT NULL DEFAULT '0' COMMENT '隐藏',
  `in_layout` tinyint NOT NULL DEFAULT '1' COMMENT '是否内部',
  `auth` tinyint NOT NULL DEFAULT '0' COMMENT '参与授权',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路由菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_menu` DISABLE KEYS */;
INSERT INTO `core_menu` VALUES (1,0,2,'workbranch','workbranch',1,NULL,'/workbranch',0,1,1),(2,0,2,'panel','visualized/view/panel',2,NULL,'/panel',0,1,1),(3,0,2,'screen','visualized/view/screen',3,NULL,'/screen',0,1,1),(4,0,1,'data',NULL,5,NULL,'/data',0,1,0),(5,4,2,'dataset','visualized/data/dataset',1,NULL,'/dataset',0,1,1),(6,4,2,'datasource','visualized/data/datasource',2,NULL,'/datasource',0,1,1),(11,0,2,'dataset-form','visualized/data/dataset/form',7,NULL,'/dataset-form',1,0,0),(12,0,2,'datasource-form','visualized/data/datasource/form',7,NULL,'/ds-form',1,0,0),(15,0,1,'sys-setting',NULL,6,NULL,'/sys-setting',1,1,0),(16,15,2,'parameter','system/parameter',1,'sys-parameter','/parameter',0,1,0),(19,0,2,'template-market','template-market',4,NULL,'/template-market',1,1,0),(30,0,1,'toolbox',NULL,7,'icon_template','/toolbox',1,1,0),(31,30,2,'template-setting','toolbox/template-setting',1,'icon_template','/template-setting',0,1,1),(64,15,2,'font','system/font',10,'icon_font','/font',0,1,0),(66,0,2,'association','visualized/data/lineage',4,'association','/lineage',0,1,1),(67,15,2,'share-management','system/share',2,'icon_share-label_outlined','/share-management',0,1,1),(68,15,2,'site-setting','system/site',3,'tab-title','/site-setting',0,1,1),(69,15,2,'user-management','system/user',4,'icon_member_filled','/user-management',0,1,1),(70,0,1,'msg',NULL,200,NULL,'/msg',1,1,0);
/*!40000 ALTER TABLE `core_menu` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_opt_recent` (
  `id` bigint NOT NULL COMMENT 'ID',
  `resource_id` bigint DEFAULT NULL COMMENT '资源ID',
  `resource_name` varchar(255) DEFAULT NULL COMMENT '资源名称',
  `uid` bigint NOT NULL COMMENT '用户ID',
  `resource_type` int NOT NULL COMMENT '资源类型 1-可视化资源 2-仪表板 3-数据大屏 4-数据集 5-数据源 6-模板',
  `opt_type` int DEFAULT NULL COMMENT '1 新建 2 修改',
  `time` bigint NOT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可视化资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_opt_recent` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_opt_recent` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_platform_token` (
  `id` int NOT NULL COMMENT '主键',
  `token` varchar(255) NOT NULL COMMENT '认证token',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `exp_time` bigint NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认证token信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_platform_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_platform_token` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_plugin` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '插件名称',
  `icon` longtext NOT NULL COMMENT '图标',
  `version` varchar(255) NOT NULL COMMENT '版本',
  `install_time` bigint NOT NULL COMMENT '安装时间',
  `flag` varchar(255) NOT NULL COMMENT '类型',
  `developer` varchar(255) NOT NULL COMMENT '开发者',
  `config` longtext NOT NULL COMMENT '插件配置',
  `require_version` varchar(255) NOT NULL COMMENT 'DE最低版本',
  `module_name` varchar(255) NOT NULL COMMENT '模块名称',
  `jar_name` varchar(255) NOT NULL COMMENT 'Jar包名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_plugin` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_plugin` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_rsa` (
  `id` int NOT NULL COMMENT '主键',
  `private_key` text NOT NULL COMMENT '私钥',
  `public_key` text NOT NULL COMMENT '公钥',
  `create_time` bigint NOT NULL COMMENT '生成时间',
  `aes_key` text NOT NULL COMMENT 'AES 加密算法的 key',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='rsa 密钥表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_rsa` DISABLE KEYS */;
INSERT INTO `core_rsa` VALUES (1,'MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCW8vbTBSoqB3CPxfq7OoNeKjPcaMFeL8rBz9v9O6jCHweTr4dt2TEZ/HpdaJfvtsG9gIN5HzzQqPhlNh0HDBI5vse1ebisxIyT6kM8hI/NXcLLWTs/QK8aR2EjIgnij/Pl10V2SlD93z7D0QQgjCbjl7BTJ60ljJ0oV5VH7hf7MIvPGJjKCsQdmiHVbV/UqINidjKfI7eoMSIZkhe78j2Ih7CMxP76BGpTRjaYluuk+WyXo8+PDq+8Y9K3VOpOeFn1GDBxsv2jGraceqObHetJYW7yRngVaJ/zS4P55Muo3OQAkZjuPM921BpAs3xc6MH5+tp+BAsE8lAn8YDlcPG3AgMBAAECggEACEnN+LdDYexnp1VRF5+2X+DEWTldSL0qDmtJ4oU4lnQat+a+8m6nDgy5cVCRayWnTK72SRvIfJKSlQgcHutwQsBxecr1s3LVXzfROkzzEm0wCXdcU/RKyRAbStEPJuQNmdSwqTaabN4ptZnP9rOLXRrSoTZAARxP8sugVENECNRDclJUlqB1V5CmhrMG9E+tOmS6D98bnEzUHwtGsVwoxPeHM2heKuvqb6+4vrL7syDewj3SSnD0murzSj+OEQ1xoTA96x2VaF9UY8R7Vlgg3Me+5UFEBW8+WppACVBUAXF1hCfS8GZKJxokjvmEwgcKLlDcPl86bAxnmljhUTsqnQKBgQC6J8nmnhfRlSUZ5Fcz6NZ9c7mNsfx4lfrOFfY6xwc2xwl5UlW6WXzU64ob58uuIgymnROhJGULX025l0/IG/Kd4il1hTZUYB0OTtn1N0ah6VvUbg8hyjlrR14kkQY6zoIFtjW6IuS/f+Z5DpRoFJ0Uh3bfBml7Ctt2U74qWz6YcwKBgQDPlZv2FB0Qn7iAZMsSUm5+lREyvQzRoK0IOQHcsrJRaLlJRFqYqMao9i7S2BU73gQCQzpP578EPBvPaLyngeosJZSfcCH9miguU1P76QTWawp/8gKsXBjY1v7dCie2HISztF61jWZShkF+Qd+kBN5a3PmJxItjdWyQklNN2k1krQKBgA0INA49CxXVBx/k6GXt6vYlX9wXlcErXr+GshIzVMH09SjVJsNA+Ig/ZQ/HDm9GWvRfEry5O4msRlizyxYLPtTmvFhJ1x/ZCq+xvXh5QiyblZ4thkmrSpHEEUcmD/WV92xG8UPypWjyn0PiTWlr6AND6IJ8hG3kuLBz9GAquxsZAoGBAIY3j52uJ5TGEL9DbGGuVq8VpwcNYSpxHy+/3EpcaBtnANY0kfc+uSQp+OBuwlvxEtp7qUpc5w7P5pNWfxQ3VbexnMTx9ahJck1xp02rvmr8zs8nJT9YVOBvMl6s4DxJeCvRJaNxh4U4NURzRyx/0V0in13KbO2j0MwxMBeYXI4JAoGALVeeqWPxjBvyi+IbFYo9DLkohdi1fc+3wCGgLhmdPOpAX+UrtJoEZdEEj6sdjMaSoGrpspV34yenLpyHKF/KxbFGtzr+hjqg2sn6o7W/v8VGKd0rKGnFmyvpHnOl9KJOZvYl2tscivjcLgI/LvGyDW3Fpm908fVbQLlJy8RQdfs=','MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlvL20wUqKgdwj8X6uzqDXioz3GjBXi/Kwc/b/Tuowh8Hk6+HbdkxGfx6XWiX77bBvYCDeR880Kj4ZTYdBwwSOb7HtXm4rMSMk+pDPISPzV3Cy1k7P0CvGkdhIyIJ4o/z5ddFdkpQ/d8+w9EEIIwm45ewUyetJYydKFeVR+4X+zCLzxiYygrEHZoh1W1f1KiDYnYynyO3qDEiGZIXu/I9iIewjMT++gRqU0Y2mJbrpPlsl6PPjw6vvGPSt1TqTnhZ9RgwcbL9oxq2nHqjmx3rSWFu8kZ4FWif80uD+eTLqNzkAJGY7jzPdtQaQLN8XOjB+frafgQLBPJQJ/GA5XDxtwIDAQAB',1779664249927,'KRk7PfSPIrpj1ubO');
/*!40000 ALTER TABLE `core_rsa` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_setting_authentication` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `type` varchar(10) NOT NULL COMMENT '类型',
  `enable` tinyint NOT NULL COMMENT '是否启用',
  `sync_time` bigint NOT NULL COMMENT '同步时间',
  `relational_ids` varchar(255) DEFAULT NULL COMMENT '相关的ID',
  `plugin_json` longtext COMMENT '插件配置',
  `synced` tinyint NOT NULL DEFAULT '0' COMMENT '已同步',
  `valid` tinyint NOT NULL DEFAULT '0' COMMENT '有效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认证设置';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_setting_authentication` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_setting_authentication` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_share` (
  `id` bigint NOT NULL COMMENT 'ID',
  `creator` bigint NOT NULL COMMENT '创建人',
  `time` bigint NOT NULL COMMENT '创建时间',
  `exp` bigint DEFAULT NULL COMMENT '过期时间',
  `uuid` varchar(16) NOT NULL COMMENT 'uuid',
  `pwd` varchar(255) DEFAULT NULL COMMENT '密码',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `oid` bigint NOT NULL COMMENT '组织ID',
  `type` int NOT NULL COMMENT '业务类型',
  `auto_pwd` tinyint NOT NULL DEFAULT '1' COMMENT '自动生成密码',
  `ticket_require` tinyint NOT NULL DEFAULT '0' COMMENT 'ticket必须',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共链接';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_share` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_share` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_share_ticket` (
  `id` bigint NOT NULL COMMENT 'ID',
  `uuid` varchar(255) NOT NULL COMMENT '分享uuid',
  `ticket` varchar(255) NOT NULL COMMENT 'ticket',
  `exp` bigint DEFAULT NULL COMMENT 'ticket有效期',
  `args` longtext COMMENT 'ticket参数',
  `access_time` bigint DEFAULT NULL COMMENT '首次访问时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分享Ticket表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_share_ticket` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_share_ticket` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_store` (
  `id` bigint NOT NULL COMMENT 'ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `uid` bigint NOT NULL COMMENT '用户ID',
  `resource_type` int NOT NULL COMMENT '资源类型',
  `time` bigint NOT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_store` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_store` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_sys_setting` (
  `id` bigint NOT NULL COMMENT 'ID',
  `pkey` varchar(255) NOT NULL COMMENT '键',
  `pval` varchar(255) NOT NULL COMMENT '值',
  `type` varchar(255) NOT NULL COMMENT '类型',
  `sort` int NOT NULL DEFAULT '0' COMMENT '顺序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_sys_setting` DISABLE KEYS */;
INSERT INTO `core_sys_setting` VALUES (1,'basic.dsIntervalTime','6','text',11),(2,'basic.dsExecuteTime','minute','text',3),(3,'ai.baseUrl','','text',0),(7,'template.url','','text',0),(8,'template.accessKey','crest','text',1),(9,'basic.frontTimeOut','60','text',1),(10,'basic.exportFileLiveTime','30','text',2),(100102206800000001,'basic.siteTitle','Crest','text',9),(1048232869488627717,'basic.shareDisable','false','text',11),(1048232869488627718,'basic.sharePeRequire','false','text',12),(1048232869488627719,'basic.defaultSort','1','text',13),(1048232869488627720,'basic.defaultOpen','0','text',14);
/*!40000 ALTER TABLE `core_sys_setting` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_sys_startup_job` (
  `id` varchar(64) NOT NULL COMMENT 'ID',
  `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `status` varchar(255) DEFAULT NULL COMMENT '任务状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目启动任务';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_sys_startup_job` DISABLE KEYS */;
INSERT INTO `core_sys_startup_job` VALUES ('chartFilterDynamic','chartFilterDynamic','done'),('chartFilterMerge','chartFilterMerge','done'),('datasetCrossListener','datasetCrossListener','done');
/*!40000 ALTER TABLE `core_sys_startup_job` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_threshold_info` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(255) NOT NULL COMMENT '告警名称',
  `enable` tinyint NOT NULL COMMENT '是否启用',
  `rate_type` int NOT NULL COMMENT '频率类型',
  `rate_value` varchar(255) NOT NULL COMMENT '频率值',
  `resource_type` varchar(50) NOT NULL COMMENT '资源类型',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `chart_type` varchar(255) NOT NULL COMMENT '图表类型',
  `chart_id` bigint NOT NULL COMMENT '图表ID',
  `threshold_rules` longtext COMMENT '告警规则',
  `recisetting` varchar(50) NOT NULL DEFAULT '0' COMMENT '消息渠道',
  `reci_users` longtext COMMENT '接收人',
  `reci_roles` longtext COMMENT '接收角色',
  `reci_emails` longtext COMMENT '接收邮箱',
  `reci_lark_groups` longtext COMMENT '飞书群聊',
  `reci_larksuite_groups` longtext COMMENT '国际飞书群',
  `reci_webhooks` longtext COMMENT 'Web hooks',
  `msg_title` varchar(255) NOT NULL COMMENT '消息标题',
  `msg_type` int NOT NULL DEFAULT '0' COMMENT '消息类型',
  `msg_content` longtext COMMENT '消息内容',
  `repeat_send` tinyint NOT NULL DEFAULT '1' COMMENT '是否重复发送',
  `show_field_value` tinyint NOT NULL DEFAULT '0' COMMENT '显示字段值',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '数据状态',
  `creator` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(255) NOT NULL COMMENT '创建人名称',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `oid` bigint NOT NULL COMMENT '所属组织',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='告警信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_threshold_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_threshold_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_threshold_instance` (
  `id` bigint NOT NULL COMMENT '主键',
  `task_id` bigint NOT NULL COMMENT '阈值信息ID',
  `exec_time` bigint NOT NULL COMMENT '检测时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '数据状态',
  `content` longtext COMMENT '通知内容',
  `msg` longtext COMMENT '报错信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='告警实例表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_threshold_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_threshold_instance` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `core_webhook` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `url` varchar(255) NOT NULL COMMENT 'url',
  `content_type` varchar(255) NOT NULL COMMENT 'content_type',
  `secret` varchar(255) DEFAULT NULL COMMENT '密钥',
  `ssl` tinyint NOT NULL DEFAULT '0' COMMENT '开启ssl',
  `oid` bigint NOT NULL COMMENT '组织ID',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `core_webhook` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_webhook` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crest_user` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(64) NOT NULL COMMENT '账号',
  `name` varchar(128) NOT NULL COMMENT '姓名',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `phone_prefix` varchar(16) DEFAULT NULL COMMENT '电话区号',
  `phone` varchar(64) DEFAULT NULL COMMENT '电话',
  `password_hash` varchar(64) NOT NULL COMMENT '密码摘要',
  `enable` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用',
  `is_admin` tinyint NOT NULL DEFAULT '0' COMMENT '是否管理员',
  `origin` int NOT NULL DEFAULT '0' COMMENT '用户来源',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_user_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Crest用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `crest_user` DISABLE KEYS */;
INSERT INTO `crest_user` VALUES (1,'admin','管理员',NULL,NULL,NULL,'21232f297a57a5a743894a0e4a801fc3',1,1,0,1779664240000,1779664240000);
/*!40000 ALTER TABLE `crest_user` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `data_visualization_info` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `pid` varchar(50) DEFAULT NULL COMMENT '父id',
  `org_id` varchar(50) DEFAULT NULL COMMENT '所属组织id',
  `level` int DEFAULT NULL COMMENT '层级',
  `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
  `type` varchar(50) DEFAULT NULL COMMENT '类型',
  `canvas_style_data` longtext COMMENT '样式数据',
  `component_data` longtext COMMENT '组件数据',
  `mobile_layout` tinyint DEFAULT '0' COMMENT '移动端布局0-关闭 1-开启',
  `status` int DEFAULT '1' COMMENT '状态 0-未发布 1-已发布',
  `self_watermark_status` int DEFAULT '0' COMMENT '是否单独打开水印 0-关闭 1-开启',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `source` varchar(255) DEFAULT NULL COMMENT '数据来源',
  `delete_flag` tinyint DEFAULT '0' COMMENT '删除标志',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `delete_by` varchar(255) DEFAULT NULL COMMENT '删除人',
  `version` int DEFAULT '3' COMMENT '可视化资源版本',
  `content_id` varchar(50) DEFAULT '0' COMMENT '内容标识',
  `check_version` varchar(50) DEFAULT '1' COMMENT '内容检查标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可视化大屏信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `data_visualization_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `data_visualization_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `de_template_version` (
  `installed_rank` int NOT NULL COMMENT '主键',
  `version` varchar(50) DEFAULT NULL COMMENT '版本',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `type` varchar(20) DEFAULT NULL COMMENT '类型',
  `script` varchar(1000) NOT NULL COMMENT '脚本',
  `checksum` int DEFAULT NULL COMMENT 'CheckSum校验码',
  `installed_by` varchar(100) DEFAULT NULL COMMENT '安装人',
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
  `execution_time` int DEFAULT NULL COMMENT '执行时间',
  `success` tinyint NOT NULL COMMENT '执行状态',
  PRIMARY KEY (`installed_rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模板配置版本记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `de_template_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `de_template_version` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_core_chart_view` (
  `id` bigint NOT NULL COMMENT 'ID',
  `title` varchar(1024) DEFAULT NULL COMMENT '标题',
  `scene_id` bigint NOT NULL COMMENT '场景ID chart_type为private的时候 是仪表板id',
  `table_id` bigint DEFAULT NULL COMMENT '数据集表ID',
  `type` varchar(50) DEFAULT NULL COMMENT '图表类型',
  `render` varchar(50) DEFAULT NULL COMMENT '图表渲染方式',
  `result_count` int DEFAULT NULL COMMENT '展示结果',
  `result_mode` varchar(50) DEFAULT NULL COMMENT '展示模式',
  `x_axis` longtext COMMENT '横轴field',
  `x_axis_ext` longtext COMMENT 'table-row',
  `y_axis` longtext COMMENT '纵轴field',
  `y_axis_ext` longtext COMMENT '副轴',
  `ext_stack` longtext COMMENT '堆叠项',
  `ext_bubble` longtext COMMENT '气泡大小',
  `ext_label` longtext COMMENT '动态标签',
  `ext_tooltip` longtext COMMENT '动态提示',
  `custom_attr` longtext COMMENT '图形属性',
  `custom_attr_mobile` longtext COMMENT '图形属性_移动端',
  `custom_style` longtext COMMENT '组件样式',
  `custom_style_mobile` longtext COMMENT '组件样式_移动端',
  `custom_filter` longtext COMMENT '结果过滤',
  `drill_fields` longtext COMMENT '钻取字段',
  `senior` longtext COMMENT '高级',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `snapshot` longtext COMMENT '缩略图 ',
  `style_priority` varchar(255) DEFAULT 'panel' COMMENT '样式优先级 panel 仪表板 view 图表',
  `chart_type` varchar(255) DEFAULT 'private' COMMENT '图表类型 public 公共 历史可复用的图表，private 私有 专属某个仪表板',
  `is_plugin` bit(1) DEFAULT NULL COMMENT '是否插件',
  `data_from` varchar(255) DEFAULT 'dataset' COMMENT '数据来源 template 模板数据 dataset 数据集数据',
  `view_fields` longtext COMMENT '图表字段集合',
  `refresh_view_enable` tinyint DEFAULT '0' COMMENT '是否开启刷新',
  `refresh_unit` varchar(255) DEFAULT 'minute' COMMENT '刷新时间单位',
  `refresh_time` int DEFAULT '5' COMMENT '刷新时间',
  `linkage_active` tinyint DEFAULT '0' COMMENT '是否开启联动',
  `jump_active` tinyint DEFAULT '0' COMMENT '是否开启跳转',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制ID',
  `aggregate` bit(1) DEFAULT NULL COMMENT '区间条形图开启时间纬度开启聚合',
  `flow_map_start_name` longtext COMMENT '流向地图起点名称field',
  `flow_map_end_name` longtext COMMENT '流向地图终点名称field',
  `ext_color` longtext COMMENT '颜色维度field',
  `sort_priority` longtext COMMENT '字段排序优先级',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_core_chart_view` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_core_chart_view` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_data_visualization_info` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `pid` varchar(50) DEFAULT NULL COMMENT '父id',
  `org_id` varchar(50) DEFAULT NULL COMMENT '所属组织id',
  `level` int DEFAULT NULL COMMENT '层级',
  `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
  `type` varchar(50) DEFAULT NULL COMMENT '类型',
  `canvas_style_data` longtext COMMENT '样式数据',
  `component_data` longtext COMMENT '组件数据',
  `mobile_layout` tinyint DEFAULT '0' COMMENT '移动端布局0-关闭 1-开启',
  `status` int DEFAULT '1' COMMENT '状态 0-未发布 1-已发布',
  `self_watermark_status` int DEFAULT '0' COMMENT '是否单独打开水印 0-关闭 1-开启',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `source` varchar(255) DEFAULT NULL COMMENT '数据来源',
  `delete_flag` tinyint DEFAULT '0' COMMENT '删除标志',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `delete_by` varchar(255) DEFAULT NULL COMMENT '删除人',
  `version` int DEFAULT '3' COMMENT '可视化资源版本',
  `content_id` varchar(50) DEFAULT '0' COMMENT '内容标识',
  `check_version` varchar(50) DEFAULT '1' COMMENT '内容检查标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_data_visualization_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_data_visualization_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_link_jump` (
  `id` bigint NOT NULL COMMENT '主键',
  `source_dv_id` bigint DEFAULT NULL COMMENT '源仪表板ID',
  `source_view_id` bigint DEFAULT NULL COMMENT '源图表ID',
  `link_jump_info` varchar(4000) DEFAULT NULL COMMENT '跳转信息',
  `checked` tinyint DEFAULT NULL COMMENT '是否启用',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_link_jump` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_link_jump` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_link_jump_info` (
  `id` bigint NOT NULL COMMENT '主键',
  `link_jump_id` bigint DEFAULT NULL COMMENT 'link jump ID',
  `link_type` varchar(255) DEFAULT NULL COMMENT '关联类型 inner 内部仪表板，outer 外部链接',
  `jump_type` varchar(255) DEFAULT NULL COMMENT '跳转类型 _blank 新开页面 _self 当前窗口',
  `target_dv_id` bigint DEFAULT NULL COMMENT '关联仪表板ID',
  `source_field_id` bigint DEFAULT NULL COMMENT '字段ID',
  `content` varchar(4000) DEFAULT NULL COMMENT '内容 linkType = outer时使用',
  `checked` tinyint DEFAULT NULL COMMENT '是否可用',
  `attach_params` tinyint DEFAULT NULL COMMENT '是否附加点击参数',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  `window_size` varchar(255) DEFAULT 'middle' COMMENT '窗口大小large middle small',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_link_jump_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_link_jump_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_link_jump_target_view_info` (
  `target_id` bigint NOT NULL COMMENT '主键',
  `link_jump_info_id` bigint DEFAULT NULL COMMENT 'visualization_link_jump_info 表的 ID',
  `source_field_active_id` bigint DEFAULT NULL COMMENT '勾选字段设置的匹配字段，也可以不是勾选字段本身',
  `target_view_id` varchar(50) DEFAULT NULL COMMENT '目标图表ID',
  `target_field_id` varchar(50) DEFAULT NULL COMMENT '目标字段ID',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  `target_type` varchar(50) DEFAULT 'view' COMMENT '联动目标类型 view 图表 filter 过滤组件 outParams 外部参数',
  PRIMARY KEY (`target_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_link_jump_target_view_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_link_jump_target_view_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_linkage` (
  `id` bigint NOT NULL COMMENT '主键',
  `dv_id` bigint DEFAULT NULL COMMENT '联动大屏/仪表板ID',
  `source_view_id` bigint DEFAULT NULL COMMENT '源图表id',
  `target_view_id` bigint DEFAULT NULL COMMENT '联动图表id',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `update_people` varchar(255) DEFAULT NULL COMMENT '更新人',
  `linkage_active` tinyint DEFAULT '0' COMMENT '是否启用关联',
  `ext1` varchar(2000) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(2000) DEFAULT NULL COMMENT '扩展字段2',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_linkage` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_linkage` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_linkage_field` (
  `id` bigint NOT NULL COMMENT '主键',
  `linkage_id` bigint DEFAULT NULL COMMENT '联动ID',
  `source_field` bigint DEFAULT NULL COMMENT '源图表字段',
  `target_field` bigint DEFAULT NULL COMMENT '目标图表字段',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_linkage_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_linkage_field` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_outer_params` (
  `params_id` varchar(50) NOT NULL COMMENT '主键',
  `visualization_id` varchar(50) DEFAULT NULL COMMENT '可视化资源ID',
  `checked` tinyint DEFAULT NULL COMMENT '是否启用外部参数标识（1-是，0-否）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `copy_from` varchar(50) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`params_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_outer_params` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_outer_params` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_outer_params_info` (
  `params_info_id` varchar(50) NOT NULL COMMENT '主键',
  `params_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params 表的 ID',
  `param_name` varchar(255) DEFAULT NULL COMMENT '参数名',
  `checked` tinyint DEFAULT NULL COMMENT '是否启用',
  `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
  `required` tinyint DEFAULT '0' COMMENT '是否必填',
  `default_value` varchar(255) DEFAULT NULL COMMENT '默认值 JSON格式',
  `enabled_default` tinyint DEFAULT '0' COMMENT '是否启用默认值',
  PRIMARY KEY (`params_info_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_outer_params_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_outer_params_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snapshot_visualization_outer_params_target_view_info` (
  `target_id` varchar(50) NOT NULL COMMENT '主键',
  `params_info_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params_info 表的 ID',
  `target_view_id` varchar(50) DEFAULT NULL COMMENT '联动视图ID/联动过滤项ID',
  `target_field_id` varchar(50) DEFAULT NULL COMMENT '联动字段ID',
  `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
  `target_ds_id` varchar(50) DEFAULT NULL COMMENT '联动数据集id/联动过滤组件id',
  `match_mode` varchar(255) DEFAULT 'self' COMMENT '匹配方式',
  PRIMARY KEY (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `snapshot_visualization_outer_params_target_view_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_visualization_outer_params_target_view_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_background` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `classification` varchar(255) NOT NULL COMMENT '分类名',
  `content` longtext COMMENT '内容',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT NULL COMMENT '排序',
  `upload_time` bigint DEFAULT NULL COMMENT '上传时间',
  `base_url` varchar(255) DEFAULT NULL COMMENT '所在目录地址',
  `url` varchar(255) DEFAULT NULL COMMENT '图片url',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='边框背景表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_background` DISABLE KEYS */;
INSERT INTO `visualization_background` VALUES ('board_1','1','default','',NULL,NULL,NULL,'img/board','board/board_1.svg'),('board_2','2','default',NULL,NULL,NULL,NULL,'img/board','board/board_2.svg'),('board_3','3','default',NULL,NULL,NULL,NULL,'img/board','board/board_3.svg'),('board_4','4','default',NULL,NULL,NULL,NULL,'img/board','board/board_4.svg'),('board_5','5','default',NULL,NULL,NULL,NULL,'img/board','board/board_5.svg'),('board_6','6','default',NULL,NULL,NULL,NULL,'img/board','board/board_6.svg'),('board_7','7','default',NULL,NULL,NULL,NULL,'img/board','board/board_7.svg'),('board_8','8','default',NULL,NULL,NULL,NULL,'img/board','board/board_8.svg'),('board_9','9','default',NULL,NULL,NULL,NULL,'img/board','board/board_9.svg');
/*!40000 ALTER TABLE `visualization_background` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_background_image` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `classification` varchar(255) NOT NULL COMMENT '分类名',
  `content` longtext COMMENT '内容',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT NULL COMMENT '排序',
  `upload_time` bigint DEFAULT NULL COMMENT '上传时间',
  `base_url` varchar(255) DEFAULT NULL COMMENT '所在目录地址',
  `url` varchar(255) DEFAULT NULL COMMENT '图片url',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='背景图';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_background_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_background_image` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_link_jump` (
  `id` bigint NOT NULL COMMENT '主键',
  `source_dv_id` bigint DEFAULT NULL COMMENT '源仪表板ID',
  `source_view_id` bigint DEFAULT NULL COMMENT '源图表ID',
  `link_jump_info` varchar(4000) DEFAULT NULL COMMENT '跳转信息',
  `checked` tinyint DEFAULT NULL COMMENT '是否启用',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跳转记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_link_jump` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_link_jump` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_link_jump_info` (
  `id` bigint NOT NULL COMMENT '主键',
  `link_jump_id` bigint DEFAULT NULL COMMENT 'link jump ID',
  `link_type` varchar(255) DEFAULT NULL COMMENT '关联类型 inner 内部仪表板，outer 外部链接',
  `jump_type` varchar(255) DEFAULT NULL COMMENT '跳转类型 _blank 新开页面 _self 当前窗口',
  `target_dv_id` bigint DEFAULT NULL COMMENT '关联仪表板ID',
  `source_field_id` bigint DEFAULT NULL COMMENT '字段ID',
  `content` varchar(4000) DEFAULT NULL COMMENT '内容 linkType = outer时使用',
  `checked` tinyint DEFAULT NULL COMMENT '是否可用',
  `attach_params` tinyint DEFAULT NULL COMMENT '是否附加点击参数',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  `window_size` varchar(255) DEFAULT 'middle' COMMENT '窗口大小large middle small',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跳转配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_link_jump_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_link_jump_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_link_jump_target_view_info` (
  `target_id` bigint NOT NULL COMMENT '主键',
  `link_jump_info_id` bigint DEFAULT NULL COMMENT 'visualization_link_jump_info 表的 ID',
  `source_field_active_id` bigint DEFAULT NULL COMMENT '勾选字段设置的匹配字段，也可以不是勾选字段本身',
  `target_view_id` varchar(50) DEFAULT NULL COMMENT '目标图表ID',
  `target_field_id` varchar(50) DEFAULT NULL COMMENT '目标字段ID',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  `target_type` varchar(50) DEFAULT 'view' COMMENT '联动目标类型 view 图表 filter 过滤组件 outParams 外部参数',
  PRIMARY KEY (`target_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跳转目标仪表板图表字段配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_link_jump_target_view_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_link_jump_target_view_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_linkage` (
  `id` bigint NOT NULL COMMENT '主键',
  `dv_id` bigint DEFAULT NULL COMMENT '联动大屏/仪表板ID',
  `source_view_id` bigint DEFAULT NULL COMMENT '源图表id',
  `target_view_id` bigint DEFAULT NULL COMMENT '联动图表id',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `update_people` varchar(255) DEFAULT NULL COMMENT '更新人',
  `linkage_active` tinyint DEFAULT '0' COMMENT '是否启用关联',
  `ext1` varchar(2000) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(2000) DEFAULT NULL COMMENT '扩展字段2',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联动记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_linkage` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_linkage` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_linkage_field` (
  `id` bigint NOT NULL COMMENT '主键',
  `linkage_id` bigint DEFAULT NULL COMMENT '联动ID',
  `source_field` bigint DEFAULT NULL COMMENT '源图表字段',
  `target_field` bigint DEFAULT NULL COMMENT '目标图表字段',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `copy_from` bigint DEFAULT NULL COMMENT '复制来源',
  `copy_id` bigint DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联动字段';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_linkage_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_linkage_field` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_outer_params` (
  `params_id` varchar(50) NOT NULL COMMENT '主键',
  `visualization_id` varchar(50) DEFAULT NULL COMMENT '可视化资源ID',
  `checked` tinyint DEFAULT NULL COMMENT '是否启用外部参数标识（1-是，0-否）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `copy_from` varchar(50) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`params_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部参数关联关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_outer_params` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_outer_params` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_outer_params_info` (
  `params_info_id` varchar(50) NOT NULL COMMENT '主键',
  `params_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params 表的 ID',
  `param_name` varchar(255) DEFAULT NULL COMMENT '参数名',
  `checked` tinyint DEFAULT NULL COMMENT '是否启用',
  `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
  `required` tinyint DEFAULT '0' COMMENT '是否必填',
  `default_value` varchar(255) DEFAULT NULL COMMENT '默认值 JSON格式',
  `enabled_default` tinyint DEFAULT '0' COMMENT '是否启用默认值',
  PRIMARY KEY (`params_info_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_outer_params_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_outer_params_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_outer_params_target_view_info` (
  `target_id` varchar(50) NOT NULL COMMENT '主键',
  `params_info_id` varchar(50) DEFAULT NULL COMMENT 'visualization_outer_params_info 表的 ID',
  `target_view_id` varchar(50) DEFAULT NULL COMMENT '联动视图ID/联动过滤项ID',
  `target_field_id` varchar(50) DEFAULT NULL COMMENT '联动字段ID',
  `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(50) DEFAULT NULL COMMENT '复制来源ID',
  `target_ds_id` varchar(50) DEFAULT NULL COMMENT '联动数据集id/联动过滤组件id',
  `match_mode` varchar(255) DEFAULT 'self' COMMENT '匹配方式',
  PRIMARY KEY (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部参数联动视图字段信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_outer_params_target_view_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_outer_params_target_view_info` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_report_filter` (
  `id` bigint NOT NULL COMMENT 'id',
  `report_id` bigint DEFAULT NULL COMMENT '定时报告id',
  `task_id` bigint DEFAULT NULL COMMENT '任务id',
  `resource_id` bigint DEFAULT NULL COMMENT '资源id',
  `dv_type` varchar(255) DEFAULT NULL COMMENT '资源类型',
  `component_id` bigint DEFAULT NULL COMMENT '组件id',
  `filter_id` bigint DEFAULT NULL COMMENT '过滤项id',
  `filter_info` longtext COMMENT '过滤组件内容',
  `filter_version` int DEFAULT NULL COMMENT '过滤组件版本',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) DEFAULT NULL COMMENT '创建人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时报告过自定义过滤组件信息';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_report_filter` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_report_filter` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_subject` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '主题名称',
  `type` varchar(255) DEFAULT NULL COMMENT '主题类型 system 系统主题，self 自定义主题',
  `details` longtext COMMENT '主题内容',
  `delete_flag` tinyint DEFAULT '0' COMMENT '删除标记',
  `cover_url` longtext COMMENT '封面信息',
  `create_num` int NOT NULL DEFAULT '0' COMMENT '创建序号',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `delete_by` bigint DEFAULT NULL COMMENT '删除人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主题表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_subject` DISABLE KEYS */;
INSERT INTO `visualization_subject` VALUES ('10001','chart.light_theme','system','{\"width\":1920,\"height\":1080,\"refreshViewEnable\":false,\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"scale\":60,\"scaleWidth\":100,\"scaleHeight\":100,\"backgroundColorSelect\":true,\"backgroundImageEnable\":false,\"backgroundType\":\"backgroundColor\",\"background\":\"\",\"openCommonStyle\":true,\"opacity\":1,\"fontSize\":14,\"themeId\":\"10001\",\"color\":\"#000000\",\"backgroundColor\":\"rgba(245, 246, 247, 1)\",\"dashboard\":{\"gap\":\"yes\",\"gapSize\":5,\"resultMode\":\"all\",\"resultCount\":1000,\"themeColor\":\"light\",\"mobileSetting\":{\"customSetting\":false,\"imageUrl\":null,\"backgroundType\":\"image\",\"color\":\"#000\"}},\"component\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true,\"remarkShow\":false,\"remark\":\"\",\"fontFamily\":\"Microsoft YaHei\",\"letterSpace\":\"0\",\"fontShadow\":false,\"color\":\"#000000\",\"remarkBackgroundColor\":\"#ffffff\"},\"chartColor\":{\"basicStyle\":{\"colorScheme\":\"default\",\"colors\":[\"#1E90FF\",\"#90EE90\",\"#00CED1\",\"#E2BD84\",\"#7A90E0\",\"#3BA272\",\"#2BE7FF\",\"#0A8ADA\",\"#FFD700\"],\"alpha\":100,\"gradient\":false,\"mapStyle\":\"normal\",\"areaBaseColor\":\"#FFFFFF\",\"areaBorderColor\":\"#303133\",\"gaugeStyle\":\"default\",\"tableBorderColor\":\"#E6E7E4\",\"tableScrollBarColor\":\"#00000024\"},\"misc\":{\"mapLineGradient\":false,\"mapLineSourceColor\":\"#146C94\",\"mapLineTargetColor\":\"#576CBC\",\"nameFontColor\":\"#000000\",\"valueFontColor\":\"#5470c6\"},\"tableHeader\":{\"tableHeaderBgColor\":\"#1E90FF\",\"tableHeaderFontColor\":\"#000000\"},\"tableCell\":{\"tableItemBgColor\":\"#FFFFFF\",\"tableFontColor\":\"#000000\"}},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"backgroundImageEnable\":false,\"backgroundType\":\"innerImage\",\"innerImage\":\"board/board_1.svg\",\"outerImage\":null,\"innerPadding\":12,\"borderRadius\":0,\"backgroundColor\":\"rgba(255,255,255,1)\",\"innerImageColor\":\"rgba(16, 148, 229,1)\"},\"filterStyle\":{\"layout\":\"horizontal\",\"titleLayout\":\"left\",\"labelColor\":\"#000000\",\"titleColor\":\"#000000\",\"color\":\"#000000\",\"borderColor\":\"#F3E7E7\",\"text\":\"#484747\",\"bgColor\":\"#FFFFFF\"},\"tabStyle\":{\"headPosition\":\"left\",\"headFontColor\":\"#OOOOOO\",\"headFontActiveColor\":\"#OOOOOO\",\"headBorderColor\":\"#OOOOOO\",\"headBorderActiveColor\":\"#OOOOOO\"}}}',0,'data:image/png;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBuAGMALgAgADIAMAAxADb/2wBDAAwICQoJBwwKCQoNDAwOER0TERAQESMZGxUdKiUsKyklKCguNEI4LjE/MigoOk46P0RHSktKLTdRV1FIVkJJSkf/2wBDAQwNDREPESITEyJHMCgwR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0f/wAARCABSAK4DASIAAhEBAxEB/8QAGgAAAgMBAQAAAAAAAAAAAAAAAAECAwQFB//EADgQAAIBAgQEAwUGBQUAAAAAAAECAAMRBBIhMQVBUWETUpEiMnGBoQYzQnKxwSNDYpLRFBWC8PH/xAAZAQEBAQEBAQAAAAAAAAAAAAAAAgEDBAX/xAApEQACAgEDAwMDBQAAAAAAAAAAAQIRMQMSIQQTYTIzQSJRcYGhweHw/9oADAMBAAIRAxEAPwD1WErq1PDCnKWB3tKquKNv4KXPVhoJSi2Y2kVVeJLSxVajUS3hlcpuTmuL7W5TYhZkBZcpPK95zWNdyxYIcwsfYGslTqYimLIEA3tlsPpLemyN50oTPQxBchai5W6jYy/nObTWS07HM+LxS4VabOBldwhJNrd/pLyQBc6ATPVqh1y+CGX+r/EhtIuKbHhcSuKDNTAKA2zA7maJgBqhyyhQT0EmK2IHJT8pm9FdtmyEqouzqxYWIa36S2WnZDVBMeHx3j+ylP8AiC+Zb7WNt7TQ9VUNrFj0EzuwawWglh1E2jm5JPJshMvj1re6voY6dWq1VQwUKd7DtG0xaiZphFEzKouxsJh0MuJx64bFeFVUBSmZWudTe1tppoualJXK5cwuBflKqlSg2pQufyyNOtTpJkp0GRRsFAA/WVtb+AaoTI+KqEfw6YHdjNKXyLfU21hxayCqriKCNkqsAR1BliqjKGUAgi4MeufdrW6aR85IFkXyiGRfKJKItY2Gp6CLFEci+USVtLREMwIIAB7ytqaBAjAZb82IixRZkHT5XMMo6CR8QWvmT+6BqaG1iR0N7TKNsllHQQyjoJiPEqILA1Pd39ky5cQzqGVXsbWOQ84M3GgADYWlNTEUFLB6gBXQ6kWk6bFtTm2G4sJKxDE5iQeXSaMkaRp1EzU7FesllHQSUIMpEco6CGUXBsNJKQqKjIRUtl53MG0SIuLH9ZHw16X+JJkWp0hTVrHKg0sx2H6ynh3EKHEqDVsM2ZFfLexGtgeY7ykpVawZaujTkXyiGRfKJnHEMKcQ9E1ArIbHMbCKrxHDUqqIagOc2zDYfPaKeBaNORfKI5z+J8TXA4fxh7YLhdNf+7TbRfxEDdQDJMUk3Q/5g0bbe+kkRfseoisM2bna28VyxOUkEdV0godm8w+Nor6lU5bkxFmzZCdTzCn/AMjLKllsddrKTGBY8pO7n5aQCqDcDXrzg7BFuQfkCf0jvpfX0iwOQqKGQ5gD8Y1bMLgH5giLMHDAAj4giAYK1JS9wLG3LSOgBmuC21rEzQaOZyLm47G3rEKGSpe5udNASIIpl1MaX1266RgJ4pIX2uZy/vEi5WPs8t77yetxBY4Tn8Mw+KoVsS2JxHio7A01sRkGvb4ek0lq/wDrAoyijbXe5+lvrNdfA5Odx/idfh1XBrRAIrVCrX6af5k/tQ7U/s9iWRirDJYg2PviS4zgGxr4ZlKjwmLG6k9Og7TRxXDtiuHVaCEKWtYkE8weUpayjTUOY8/n5/odrdw5cS/b/ZKeEsz/AGfw7OxZjQ1JNydJz/sSrJweqGBB8c7/AJVnYwdFqPDqdAkEqmW9iJn4Hg3wWDek7Biaha4BHIdRMfUTdx28S5fjwOzBLdu5jwvJzeHB632o4hQrAmiASoI0vcc4+LCon2j4fSp/csAHHXUzfg8E9LjOJxRYFagsBlI5jna3KGNwNSvxbC4lWAWla4sTfUnpJfU6j+tQV4rxi/yWtDSva5Os/rmjL9qhm4eov/NH6GdbCfcJ+UfpMnG8E+MwoRGCnODsTyPQTdQQpSVTyAnKO7uO8cBw01BSXq5v+Ct8TkZh4VZiOlMm8uFyLyk0qhrEjEVlU7KAmUfS8vAsACb9zOpAa9oa9o4QBa9oa9o5AF85DKoTkQ1yflaAS17Q17RwgC17Q17RxGwFzYCAGvaGvaOY3NSnilXxarhyTlsLKP7dtesA169oa9pV4lQMyincL+Im1/hpInEPlzpRJW+xuD8haAX69oa9pU1Vlrin4RKke8L6fT94Co+YqaWvI3NvW0Am7FFvYt2UXkPGOZV8OpqL+4bCLxqpFlojNzzEqPW0mzPplVDprdrftADOfKfSIVSb+w+n9O8kzN+FUPxa37fGBLXNgtrae1z9IAg5P4W9JJSSNrfESKtUN8y0x0s9/wBpJCxQFwobmFNx6wCsrUOIDCvZANaYUa977y23cyP8z3Ra3vSVxci4uOU0BbuYW7mBIAJJsBuZysVxB3YrQOVB+LmZUIOb4JlJRydW3cyqriaNLSpVAPQamcRqlRveqO3xYmRnoXT/AHZyer9kdf8A3DD+Z/7YHiGH87n/AIzkQldiJndka8VjKj1QcM1ha1nJA+kppNiKjItesNNt21+neVrvLaX3qfmE8mutkqR7unipw3M69Ck1IWaoz9zJNZ2yh2Ug65fWWSqooF2Fxffpy/xJOJMKQx5D47wZQ4s4+sogSANTaAXhQG0HKMi40NpmDA7EGOAWVjsL6yuWUtzLIBmIB3EYAG00EqNyB8YCx2sYBnl1L3JKA22tADmJyOJ6Y0kb2EITvoeo56vpMYrVX9lqrsuuhYkRwhPVDBwlkIQhLJCEIQBrvLaX3qfmEIT53U+4fV6T2md2VkA19Rf2f3hCQecnlXyj0hlXyj0hCAGVfKPSGVfKPSEIAAAbACOEIAo4QgBCEIB//9k=',0,1696427707737,NULL,NULL,NULL,NULL,NULL),('10002','chart.dark_theme','system','{\"width\":1920,\"height\":1080,\"refreshViewEnable\":false,\"refreshViewLoading\":true,\"refreshUnit\":\"minute\",\"refreshTime\":5,\"scale\":60,\"scaleWidth\":100,\"scaleHeight\":100,\"backgroundColorSelect\":true,\"backgroundImageEnable\":false,\"backgroundType\":\"backgroundColor\",\"background\":\"\",\"openCommonStyle\":true,\"opacity\":1,\"fontSize\":14,\"themeId\":\"10002\",\"color\":\"#000000\",\"backgroundColor\":\"rgba(3, 11, 46, 1)\",\"dashboard\":{\"gap\":\"yes\",\"gapSize\":5,\"resultMode\":\"all\",\"resultCount\":1000,\"themeColor\":\"dark\",\"mobileSetting\":{\"customSetting\":false,\"imageUrl\":null,\"backgroundType\":\"image\",\"color\":\"#000\"}},\"component\":{\"chartTitle\":{\"show\":true,\"fontSize\":\"18\",\"hPosition\":\"left\",\"vPosition\":\"top\",\"isItalic\":false,\"isBolder\":true,\"remarkShow\":false,\"remark\":\"\",\"fontFamily\":\"Microsoft YaHei\",\"letterSpace\":\"0\",\"fontShadow\":false,\"color\":\"#FFFFFF\",\"remarkBackgroundColor\":\"#5A5C62\"},\"chartColor\":{\"basicStyle\":{\"colorScheme\":\"default\",\"colors\":[\"#1E90FF\",\"#90EE90\",\"#00CED1\",\"#E2BD84\",\"#7A90E0\",\"#3BA272\",\"#2BE7FF\",\"#0A8ADA\",\"#FFD700\"],\"alpha\":100,\"gradient\":false,\"mapStyle\":\"darkblue\",\"areaBaseColor\":\"5470C6\",\"areaBorderColor\":\"#EBEEF5\",\"gaugeStyle\":\"default\",\"tableBorderColor\":\"#CCCCCC\",\"tableScrollBarColor\":\"#FFFFFF80\"},\"misc\":{\"mapLineGradient\":false,\"mapLineSourceColor\":\"#2F58CD\",\"mapLineTargetColor\":\"#3795BD\",\"nameFontColor\":\"#ffffff\",\"valueFontColor\":\"#5470c6\"},\"tableHeader\":{\"tableHeaderBgColor\":\"#1E90FF\",\"tableHeaderFontColor\":\"#FFFFFF\"},\"tableCell\":{\"tableItemBgColor\":\"#131E42\",\"tableFontColor\":\"#FFFFFF\"}},\"chartCommonStyle\":{\"backgroundColorSelect\":true,\"backgroundImageEnable\":false,\"backgroundType\":\"innerImage\",\"innerImage\":\"board/board_1.svg\",\"outerImage\":null,\"innerPadding\":12,\"borderRadius\":0,\"backgroundColor\":\"rgba(19,28,66,1)\",\"innerImageColor\":\"#1094E5\"},\"filterStyle\":{\"layout\":\"horizontal\",\"titleLayout\":\"left\",\"labelColor\":\"#FFFFFF\",\"titleColor\":\"#FFFFFF\",\"color\":\"#FFFFFF\",\"borderColor\":\"#484747\",\"text\":\"#AFAFAF\",\"bgColor\":\"#131C42\"},\"tabStyle\":{\"headPosition\":\"left\",\"headFontColor\":\"#FFFFFF\",\"headFontActiveColor\":\"#FFFFFF\",\"headBorderColor\":\"#131E42\",\"headBorderActiveColor\":\"#131E42\"}}}',0,'data:image/png;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBuAGMALgAgADIAMAAxADb/2wBDAAsICAoIBwsKCQoNDAsNERwSEQ8PESIZGhQcKSQrKigkJyctMkA3LTA9MCcnOEw5PUNFSElIKzZPVU5GVEBHSEX/2wBDAQwNDREPESESEiFFLicuRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUX/wAARCABSAK4DASIAAhEBAxEB/8QAGwAAAgMBAQEAAAAAAAAAAAAAAAMBAgQFBgf/xAA/EAABAwICBwMIBwkBAQAAAAABAAIDBBESIQUTMUFRYZEUItEjQlNUcYGSoSQzk6Ox4fAGFRYyUmOiwfFDYv/EABgBAQEBAQEAAAAAAAAAAAAAAAEAAgME/8QAKhEAAgIBAwEHBAMAAAAAAAAAAAECESEDEjFRBBMUIjJB8GGBscFxofH/2gAMAwEAAhEDEQA/APmbq6R8jpJO/I4klzjck8U5zaqRpmOqsRiNnt4X2X+SwWuVpNFIBfHDtt9a3xXrepLqc9qLMM0kb5G6vCzbctB6HbsXU0Z+z9ZpWnbLBLC0OubPIbsNt64bm4HFpIJHA3Cs2WRos17gOAK3CaT8+TM4tryujbX0U1BUMhe9jnPbiBGQ28/Ysj3PY9zHEXBsbWKo57nm73Fx5m6lrLi5IA4lEpW7jhDFNLzchrHcfkpxu4joryU5jYJA5skZNsbL2vwzzCpGGGRokcWsJGJwFyB7EPcsMcMMbuXRGN36C3mDReE/TJy6+R1OW/n7Pmphp9FF411dO1uw4YLm/wAWxFsaMGM/oIDjy6LdJBottO90dbO6W3cZqbAnLIm+W/PksCrZFsR/QRiKqpTYE4ipxFVUqEm5RcqFKSJuUXUKVESE6nq6ijeX008kLiLExuLSR7kkIUyM8RAmZcEjELgC9/cvRVtXoyemqBFo+VkrgwCbszWgEEeaD3b5g2JvcLzjWlzw1u0mwXTboPSD88IN95eiGlPUzBNmXOMeWdn96aGE88p0W8ukthJpWYGWcCQI72GV95J5BIptLaFg1xdSOeXz6xpfSxus247tr5DI5DbeyxN/ZzSbjkxv2gWWPRNVK6RrGAmN5Y7vbxtSuzat1tZPUhymbtMaR0XVUEcVDTOjmbJic4wRsxDvXzab722GwW3rBSw4oxNJG59OwFrywBxacyMr5bs/an/w/X3+raPa4J8GgNKxyB1OMD9gLJLH5La7NqrmLDvI9TNUNYyCWSNr2wyta2PGzCXOBBJtc5DMX5rnrqaT0RpKkaZ9IAk4g0l77uuuYszTTSaoY5yizwA/INtyJIUZcleUHGbh3vZh+SrY8D0WDRGXJT0RhPPomak9n1t8sWG1uV1JWTYvLkpBsb2CLHgeinCbXsbexRF3zOkaGuwWHBoH4KmXJRY8+icyIOp5ZDfE0tAy438FpK+C4FZclOXJFjwPRPhYDT1BLblrRY22d4JSsG6EdE6GNsjJifMZiFuNwP8AaVb29Foph5Op2/VcP/pqYK2T4M/RCm1v+IWREQkNnjJNgHC59693T6foWRtY6Y5NDT9IIBy4LwTWlzg1oJJNgBvVzDICQY3gt2jCcl10O0vRTildnKelvdn0H986LLWiOeKMjaTLe64ujNKUtNPWl8uUkzy3DLhyJyPNeWLS21wRcXF1C7+PluUq/JnuFVWfQv4h0dI0tfIS027pqSR+CKXS2jQ/ytZEBbjfPnmF8/axz3YWNLjwAurPhkjF3xvaDvcCFrx06xH8me4jeWew/avSlFW6NZHS1DJCxzRkcyM93vt7l41CACSABcncvHqaj1Jbmd4R2qiXWJ7oAHC6cQ3scdgMeN1zbdYWz6pb4ZIyWvjc0g2ILSLKCHDJwI5FYTwaYJ9x2DDv1t/ks6m5ta+Sk6BqybexPa4dge3eZWn5HxWdTns3JToXkOi0RuApJmnaXNI+azoubWvklOgasnotEJAp6gHaWi3ULMFa5AyO1SdC1ZPRPpyAye++Ow6jwS44JZReOJ7xxa0lVc18bi1wc128EWKVayDp4BChSsiIje+KRskbi17SC0jaCug3S+lY3F3aJcRc513C+ZILto5Bc5mT23sc967cs1Y2R2Oqgc7E8kiuJucr54s75e23Jc0k8k3WDjSySSvxSkl3Eqq11Mo1x1scUrzmXCRzr++6Vro/V2fE7xWqXULCmqpqOYTU0hjkGxwT63S9fpFjWVlS+ZrTcB25I1sfq7OrvFGtj9Azq7xW02lW7H3ClzQSzTyNAmfI5oOWIlLBLXAg2IzBC01T3vZGXva7gBIXEdTksqwaOoNM6Wa57hUTYpHYnOw5uJ525DosVTUT1UgkqXFz7WBItlf/AKuprKrBGO0wYThsO2O7owm1+9lYXHK9liqZrvBmZHK4jbrXO/2pRSSBuzEpTtdF6tH8TvFGuj9Wj+J3itUuoWKQna6P1dnxO8Ua2P1dnxO8U0uo2JQE7Wx+rs6u8Ua2P1dnV3iqvqVikJutj9Azq7xRrY/QM6u8U0uo2PpNKVtCwspamSJpNyGnely1lRNUOqJJnmZ2198yq61noGdXeKNYz0LOp8VpttU2FLmiJHSSOL5S5zic3ONyVVaZ3udTsxOYW5BrRIXEe6+Sz7lhiZmXxtwmxuLG9re9dcsh9dqhmf8A0jOWVvP9vyXIbbGL7L55XXVkLHPLuwYRd2XZnDhl/Pu/2uaWLshE4lEnkKl7mcXzNB+Tkv6V6f74eKtLC2WQFsMsd7ANZCbHq4qhpLebUfY/mt/OTJQyTg2MzvtPzVe+fO/yV+yy3I1UtwLnyZ2INNI0EmOUAC5JYs3ZpIJhGGM1bpHO87FawPKxSU+c3azyIjAFrhpGLqUlRHXayPCMVZUg5XtKw+bn5/G3uSZmygjUVMjhbPHM0Z+5xTLswsd2IlrcOfZzn3d/e37fckSwsfY6qWO2RwwHPq7kVvh1f9mXn/Ctqv0/348UWqvT/fjxUdkAP8tR9j+aOyi4GGoudg1O35pv5YUTaq9P98PFH0n033w8UCkvazag3/s/mlujia4hzpARtBjHim/liX+k+m++HioL5wbGV3uk/NUwxf1v+AeKrZvE9FNikM1k3pXfGoJkcbl9zzcq2bxPRTZvE9Fm2xJs7+r/ACR3/wCr/JRZvE9EWbxPRRDXhmqbZzy/eCRb8UrcnP8AqG+Swi+T8JF8uN0lRGUGxuNq1S11WXm9VN/MT9Yd9r/gOiELm/T9/wBMVy/4/aIGkKwZirnBH9wo7fV+tT5C31h2IQsgS3SNa0WFXUAZZCV27ZvVXVdQ9pa6olcDtBeT+tpQhKEo6R7wMb3OtkLm9lVCEoDU6tqmhoFTMAA2wDzuFh8slDa+rYbtqpwb3ykKEKl62PsiTX1bjc1U5zvnIdu1Ar6tosKqcDgJChCiA11Wb3qpjfb5Q5pTnukcXPcXOO0k3KEJQEIQhaIshCFECAhCSGOke5oa57i0bATkFVCFexH/2Q==',0,1696427762072,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `visualization_subject` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_template` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
  `level` int DEFAULT NULL COMMENT '层级',
  `dv_type` varchar(255) DEFAULT NULL COMMENT '模板种类  dataV or dashboard 目录或者文件夹',
  `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  app or template 应用 或者 模板',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `snapshot` longtext COMMENT '缩略图',
  `template_type` varchar(255) DEFAULT NULL COMMENT '模板类型 system 系统内置 self 用户自建 ',
  `template_style` longtext COMMENT 'template 样式',
  `template_data` longtext COMMENT 'template 数据',
  `dynamic_data` longtext COMMENT '预存数据',
  `app_data` longtext COMMENT 'app数据',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `version` int DEFAULT '3' COMMENT '使用资源的版本',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_template` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_template_category` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `pid` varchar(255) DEFAULT NULL COMMENT '父级id',
  `level` int DEFAULT NULL COMMENT '层级',
  `dv_type` varchar(255) DEFAULT NULL COMMENT '模板种类  dataV or dashboard 目录或者文件夹',
  `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型  folder or panel 目录或者文件夹',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `snapshot` longtext COMMENT '缩略图',
  `template_type` varchar(255) DEFAULT NULL COMMENT '模版类型 system 系统内置 self 用户自建',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_template_category` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_template_category` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_template_category_map` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `category_id` varchar(255) DEFAULT NULL COMMENT '名称',
  `template_id` varchar(255) DEFAULT NULL COMMENT '父级id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_template_category_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_template_category_map` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_template_extend_data` (
  `id` bigint NOT NULL COMMENT '主键',
  `dv_id` bigint DEFAULT NULL COMMENT '模板ID',
  `view_id` bigint DEFAULT NULL COMMENT '图表ID',
  `view_details` longtext COMMENT '图表详情',
  `copy_from` varchar(255) DEFAULT NULL COMMENT '复制来源',
  `copy_id` varchar(255) DEFAULT NULL COMMENT '复制来源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模板图表明细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_template_extend_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `visualization_template_extend_data` ENABLE KEYS */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visualization_watermark` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `version` varchar(255) DEFAULT NULL COMMENT '版本号',
  `setting_content` longtext COMMENT '设置内容',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仪表板水印设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `visualization_watermark` DISABLE KEYS */;
INSERT INTO `visualization_watermark` VALUES ('system_default','1.0','{\"enable\":false,\"enablePanelCustom\":true,\"type\":\"custom\",\"content\":\"水印\",\"watermark_color\":\"#DD1010\",\"watermark_x_space\":12,\"watermark_y_space\":36,\"watermark_fontsize\":15}','admin',NULL);
/*!40000 ALTER TABLE `visualization_watermark` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
