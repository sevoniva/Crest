package io.crest.system.manage;

import io.crest.substitute.permissions.user.CrestUserManage;
import io.crest.substitute.permissions.user.model.CrestUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreUserManageTest {

    @Mock
    private CrestUserManage crestUserManage;

    @InjectMocks
    private CoreUserManage coreUserManage;

    @Test
    public void getUserNameResolvesNameByUserId() {
        CrestUser user = new CrestUser();
        user.setId(2L);
        user.setAccount("zhangsan");
        user.setName("张三");
        when(crestUserManage.queryById(2L)).thenReturn(user);

        assertEquals("张三", coreUserManage.getUserName(2L));
    }

    @Test
    public void getUserNameFallsBackToAccountWhenNameIsBlank() {
        CrestUser user = new CrestUser();
        user.setId(3L);
        user.setAccount("lisi");
        when(crestUserManage.queryById(3L)).thenReturn(user);

        assertEquals("lisi", coreUserManage.getUserName(3L));
    }

    @Test
    public void getUserNameReturnsNullForEmptyUser() {
        assertNull(coreUserManage.getUserName(null));
        when(crestUserManage.queryById(4L)).thenReturn(null);

        assertNull(coreUserManage.getUserName(4L));
    }
}
