package com.portfolio.configs;

import com.portfolio.models.dao.LocationDao;
import com.portfolio.models.entity.Location;
import com.portfolio.service.DataBaseService;
import com.portfolio.util.SftpUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AfterInitializeSpring implements ApplicationListener<ContextRefreshedEvent> {
    private final SftpUtil sftpUtil;
    private final LocationDao locationDao;
    @Value("${spring.sftp.back.conf.root}")
    private String backConfRoot;
    @Value("${spring.sftp.root}")
    private String root;
    private final Logger logger = LoggerFactory.getLogger(DataBaseService.class);
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            sftpUtil.deleteAll(root + "/mapImages");
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                logger.info("Size of locations after spring initialization ====> " + locationDao.findAll().size());
                List<Location> locations = locationDao.findAll();
                int count = 0;
                for (Location location : locations) {
                    if (location.getImagePath() != null && !location.getImagePath().isEmpty()) {
                        count++;
                        sftpUtil.uploadFile(root + location.getImagePath(), new File(backConfRoot, location.getImagePath()));
                    }
                }
                logger.info("There is " + locations.size() + " of locations and " + count + " of locations have image");
                logger.info(count + " of location images are successfully uploaded to sftp server");
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
}
