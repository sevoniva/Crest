package io.crest.substitute.permissions.user;

import io.crest.api.permissions.user.dto.UserCreator;
import io.crest.exception.DEException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class CrestUserManageTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CrestUserManage crestUserManage;

    @Test
    public void createRejectsHtmlLikeDisplayName() {
        ReflectionTestUtils.setField(crestUserManage, "configuredInitialPassword", "TestInitialPassword123!");

        UserCreator creator = new UserCreator();
        creator.setAccount("safe.user");
        creator.setName("<script>alert(1)</script>");
        creator.setRoleIds(List.of(2L));
        creator.setEnable(true);

        assertThrows(DEException.class, () -> crestUserManage.create(creator));
    }
}
