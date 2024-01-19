
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.wingmoney.core.util.HttpUtil;
import com.wingmoney.core.util.S3Util;
import com.wingmoney.core.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;

public class S3RollingFileAppender<E> extends RollingFileAppender<E> {

    private boolean s3Shipping;
    private String s3Url;
    private String s3Bucket;
    private String s3AccessKey;
    private String s3SecretKey;
    private String s3RegionName;

    @Override
    public void start() {
        validate();
        super.start();
    }

    @Override
    public void rollover() {
        super.rollover();
        s3RollOver();
    }

    protected void s3RollOver() {
        if (!s3Shipping) return;
        RollingPolicy rollingPolicy = getRollingPolicy();
        if (rollingPolicy instanceof TimeBasedRollingPolicy) {
            String periodsFileName = ((TimeBasedRollingPolicy<?>) rollingPolicy).getTimeBasedFileNamingAndTriggeringPolicy().getElapsedPeriodsFileName();
            if (StringUtils.isNotEmpty(periodsFileName)) {
                File file = new File(periodsFileName);
                if (file.exists())
                    uploadToS3(file);
                else
                    addWarn("cannot shipping log file to s3 because file " + periodsFileName + " not exist");
            } else
                addWarn("cannot shipping log file to s3 because file name is empty");
        } else
            addWarn("cannot shipping log file to s3 because rollingPolicy is not an instanceof TimeBasedRollingPolicy");
    }

    protected void uploadToS3(File file) {
        try {
            String url = String.format("%s/%s/%s", s3Url, s3Bucket, file.getName());
            addInfo("full s3 url " + url);
            HttpUtil.PlainResponse response = S3Util.putS3Object(url, s3RegionName, s3AccessKey, s3SecretKey, IOUtils.toByteArray(new FileInputStream(file)));
            if (response.isSuccess())
                addInfo("shipping log file " + file.getPath() + " to s3 success, " + url);
            else
                addInfo("shipping log file " + file.getPath() + " to s3 failed, " + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            addError("expected error occurred while shipping log file " + file.getPath() + " to s3", e);
        }
    }

    private void validate() {
        if (!s3Shipping) return;
        if (!StringUtil.isValidURL(s3Url))
            throw new IllegalArgumentException("invalid s3 url " + s3Url);
        if (StringUtils.isEmpty(s3Bucket) || StringUtils.isEmpty(s3AccessKey)
                || StringUtils.isEmpty(s3SecretKey) || StringUtils.isEmpty(s3RegionName))
            throw new IllegalArgumentException("please provide all the aws s3 bucket information and credential");
        String url = String.format("%s/%s", s3Url, s3Bucket);
        addInfo("s3 url " + url + " s3 region " + s3RegionName);
    }

    public boolean isS3Shipping() {
        return s3Shipping;
    }

    public void setS3Shipping(boolean s3Shipping) {
        this.s3Shipping = s3Shipping;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3AccessKey() {
        return s3AccessKey;
    }

    public void setS3AccessKey(String s3AccessKey) {
        this.s3AccessKey = s3AccessKey;
    }

    public String getS3SecretKey() {
        return s3SecretKey;
    }

    public void setS3SecretKey(String s3SecretKey) {
        this.s3SecretKey = s3SecretKey;
    }

    public String getS3RegionName() {
        return s3RegionName;
    }

    public void setS3RegionName(String s3RegionName) {
        this.s3RegionName = s3RegionName;
    }
}
