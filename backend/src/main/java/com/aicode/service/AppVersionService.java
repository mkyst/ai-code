package com.aicode.service;

import com.aicode.entity.AppVersion;
import com.aicode.mapper.AppVersionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionMapper versionMapper;

    /**
     * Save a version snapshot when code changes
     */
    public AppVersion saveVersion(Long appId, String html, String css, String js, String title, String changeDesc) {
        // Get current max version number
        AppVersion latest = versionMapper.selectOne(
                new LambdaQueryWrapper<AppVersion>()
                        .eq(AppVersion::getAppId, appId)
                        .orderByDesc(AppVersion::getVersionNum)
                        .last("LIMIT 1"));
        int nextVersion = (latest == null) ? 1 : latest.getVersionNum() + 1;

        AppVersion version = new AppVersion();
        version.setAppId(appId);
        version.setVersionNum(nextVersion);
        version.setHtmlContent(html);
        version.setCssContent(css);
        version.setJsContent(js);
        version.setTitle(title);
        version.setChangeDesc(changeDesc != null ? changeDesc : "v" + nextVersion);
        version.setCreatedAt(LocalDateTime.now());
        versionMapper.insert(version);
        return version;
    }

    /**
     * Get version history list for an app
     */
    public List<AppVersion> getVersionHistory(Long appId) {
        return versionMapper.selectList(
                new LambdaQueryWrapper<AppVersion>()
                        .eq(AppVersion::getAppId, appId)
                        .orderByDesc(AppVersion::getVersionNum));
    }

    /**
     * Get a specific version
     */
    public AppVersion getVersion(Long versionId) {
        return versionMapper.selectById(versionId);
    }

    /**
     * Delete all versions of an app
     */
    public void deleteAllVersions(Long appId) {
        versionMapper.delete(
                new LambdaQueryWrapper<AppVersion>().eq(AppVersion::getAppId, appId));
    }
}
