package io.crest.share.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.crest.api.share.vo.TicketVO;
import io.crest.share.dao.auto.entity.CoreShareTicket;
import io.crest.share.dao.ext.po.SharePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShareExtMapper {

    @Select("""
            select
            s.id as share_id,
            v.id as resource_id,
            v.mobile_layout as ext_flag,
            v.status as ext_flag1,
            v.type,
            s.creator,
            s.time,
            s.exp,
            v.name
            from core_share s
            left join data_visualization_info v on s.resource_id = v.id
            ${ew.customSqlSegment}
            """)
    IPage<SharePO> query(IPage<SharePO> page, @Param("ew") QueryWrapper<Object> ew);

    @Select("select type from data_visualization_info where id = #{id}")
    String visualizationType(@Param("id") Long id);

    @Update("update core_share_ticket set uuid = #{ticketUuid} where uuid = #{originUuid}")
    void updateTicketUuid(@Param("originUuid") String originUuid, @Param("ticketUuid") String ticketUuid);

    @Select("""
           select * from core_share_ticket
            ${ew.customSqlSegment}
           """)
    IPage<CoreShareTicket> pager(IPage<TicketVO> page, @Param("ew") QueryWrapper<CoreShareTicket> ew);
}
