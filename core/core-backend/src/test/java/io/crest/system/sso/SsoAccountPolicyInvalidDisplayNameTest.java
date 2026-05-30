package io.crest.system.sso;

import io.crest.exception.DEException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThrows;

@RunWith(Parameterized.class)
public class SsoAccountPolicyInvalidDisplayNameTest {

    private final String displayName;

    public SsoAccountPolicyInvalidDisplayNameTest(String displayName) {
        this.displayName = displayName;
    }

    @Parameterized.Parameters(name = "{index}: name={0}")
    public static Collection<Object[]> names() {
        return Arrays.asList(new Object[][]{
                {"<script>alert(1)</script>"},
                {"张三<script>"},
                {"<img src=x onerror=alert(1)>"},
                {"Alice<Admin"},
                {"Alice>Admin"},
                {"Alice\nAdmin"},
                {"Alice\rAdmin"},
                {"Alice\tAdmin"},
                {"Alice\u0000Admin"},
                {"Alice\u001fAdmin"},
                {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"},
                {"<b>管理员</b>"},
                {"管理员<!--x-->"},
                {"管理员<!DOCTYPE html>"},
                {"管理员<?xml version=\"1.0\"?>"},
                {"管理员<script src=x></script>"},
                {"管理员<svg/onload=alert(1)>"},
                {"管理员<iframe src=x>"},
                {"管理员<object data=x>"},
                {"管理员<input autofocus>"},
                {"管理员<style>body{}</style>"},
                {"管理员<meta http-equiv=refresh>"},
                {"管理员<link rel=stylesheet>"},
                {"管理员<base href=https://evil.example>"},
                {"管理员<form action=https://evil.example>"}
        });
    }

    @Test
    public void rejectsUnsafeDisplayName() {
        assertThrows(DEException.class, () -> SsoAccountPolicy.normalizeDisplayName(displayName, "safe.account"));
    }
}
