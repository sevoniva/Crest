package io.crest.api.system.vo;

import io.crest.api.system.request.SQLBotConfigCreator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SQLBotConfigVO extends SQLBotConfigCreator implements Serializable {
}
