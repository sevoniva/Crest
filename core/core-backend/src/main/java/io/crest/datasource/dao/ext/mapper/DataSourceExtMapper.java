package io.crest.datasource.dao.ext.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.crest.datasource.dao.ext.po.Ctimestamp;
import io.crest.datasource.dao.ext.po.DataSourceNodePO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DataSourceExtMapper extends BaseMapper<DataSourceNodePO> {


    @Select("select  unix_timestamp(current_timestamp())  as currentTimestamp")
    @Results(
            id = "selectTimestamp",
            value = {
                    @Result(property = "currentTimestamp", column = "currentTimestamp")
            }
    )
    Ctimestamp selectTimestamp();

}
