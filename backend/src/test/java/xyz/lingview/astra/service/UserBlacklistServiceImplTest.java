package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.lingview.astra.service.impl.UserBlacklistServiceImpl;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBlacklistServiceImplTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserBlacklistServiceImpl blacklistService;

    @Test
    void addUserToBlacklist_shouldAddToCache() {
        blacklistService.addUserToBlacklist("banned_user");
        verify(cacheService).addToSet("astra:user:blacklist", "banned_user");
    }

    @Test
    void removeUserFromBlacklist_shouldRemoveFromCache() {
        blacklistService.removeUserFromBlacklist("banned_user");
        verify(cacheService).removeFromSet("astra:user:blacklist", "banned_user");
    }

    @Test
    void isUserInBlacklist_shouldReturnTrueWhenInSet() {
        when(cacheService.isMemberOfSet("astra:user:blacklist", "banned_user")).thenReturn(true);
        assertTrue(blacklistService.isUserInBlacklist("banned_user"));
    }

    @Test
    void isUserInBlacklist_shouldReturnFalseWhenNotInSet() {
        when(cacheService.isMemberOfSet("astra:user:blacklist", "normal_user")).thenReturn(false);
        assertFalse(blacklistService.isUserInBlacklist("normal_user"));
    }

    @Test
    void clearBlacklist_shouldDeleteSet() {
        blacklistService.clearBlacklist();
        verify(cacheService).deleteSet("astra:user:blacklist");
    }

    @Test
    void getBlacklistedUsers_shouldReturnSet() {
        Set<String> expected = Set.of("user1", "user2");
        when(cacheService.getSetMembers("astra:user:blacklist", String.class)).thenReturn(expected);
        assertEquals(expected, blacklistService.getBlacklistedUsers());
    }
}