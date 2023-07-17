package com.portfolio.util;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

public class SftpUtil {

    // Set the prompt when logging in for the first time. Optional value: (ask | yes | no)
    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private String host;

    private String username;

    private int port;

    private String password;

    private String root;

    private String privateKey;

    int timeout = 15000;

    public SftpUtil(String host, String username, String password, String privateKey, int port, String root) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.root = root;
        this.privateKey = privateKey;
    }

    private ChannelSftp createSftp() throws Exception {
        JSch jsch = new JSch();

        Session session = createSession(jsch, host, username, port);
        session.setPassword(password);
        session.connect(timeout);


        Channel channel = session.openChannel("sftp");
        channel.connect(timeout);

        return (ChannelSftp) channel;
    }


    private Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session;
        if (privateKey != null) {//키가 존재한다면
            jsch.addIdentity(privateKey);
        }
        if (port <= 0) {
            session = jsch.getSession(username, host);
        } else {
            session = jsch.getSession(username, host, port);
        }

        if (privateKey == null) {//키가 없다면
            session.setPassword(password);
        }

        if (session == null) {
            throw new Exception(host + " session is null");
        }

        Properties config = new Properties();
        config.put(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, "no");
        session.setConfig(config);
        return session;
    }

    private void disconnect(ChannelSftp sftp) {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            } else if (sftp.isClosed()) {
            }
            try {
                if (null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            } catch (JSchException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean uploadFile(String targetPath, File file) throws Exception {
        return this.uploadFile(targetPath, new FileInputStream(file));
    }

    public boolean uploadFile(String targetPath, InputStream inputStream) throws Exception {
        ChannelSftp sftp = this.createSftp();
        try {
            int index = targetPath.lastIndexOf("/");

            String fileName = targetPath.substring(index + 1);
            String dir = targetPath.substring(0, index);
            if (!dir.isEmpty()) {
                sftp.cd(dir);
            }
            sftp.put(inputStream, fileName);
            return true;
        } catch (Exception e) {
            throw new Exception("Upload File failure");
        } finally {
            this.disconnect(sftp);
        }
    }

    public boolean deleteFile(String targetPath) throws Exception {
        ChannelSftp sftp = this.createSftp();
        try {
            int index = targetPath.lastIndexOf("/");

            String targetDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            sftp.cd(targetDir);
            sftp.rm(fileName);
            return true;
        } catch (Exception e) {
            throw new Exception("Delete File failure");
        } finally {
            this.disconnect(sftp);
        }
    }

    public boolean deleteAll(String targetDirPath) throws Exception {
        ChannelSftp sftp = this.createSftp();
        try {
            sftp.cd(targetDirPath);
            if (sftp.ls("./").size() > 0) {
                sftp.rm("./*.*");
            }
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            this.disconnect(sftp);
        }
    }

    public boolean existFile(String targetPath) {
        ChannelSftp sftp = null;
        try {
            sftp = this.createSftp();
            int index = targetPath.lastIndexOf("/");

            String targetDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);

            Vector vector = sftp.ls(targetDir);
            for (Object item : vector) {
                if (item instanceof ChannelSftp.LsEntry) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) item;
                    if (entry.getFilename().equals(fileName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            this.disconnect(sftp);
        }
    }
}