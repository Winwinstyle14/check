package com.vhc.ec.notification.config;

import com.vhc.ec.notification.smpp.SmppProxyManager;
import com.vhc.ec.notification.smpp.SmsReporterImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@ConfigurationProperties(prefix = "smpp")
@Configuration
@Data
public class SmppConfig {

    public String smppId;
    public String ip;
    public Integer port;
    public String systemId;
    public String password;
    public String addressRange;

    public String systemType;

    public int numSessions;

    //public int popSMSEveryMS;
    //public int showHeartBeat;
    //public int enquireLinkInterval;
    //public int isDebugMode;

    public byte sourceTone;
    public byte sourceNpi;

    public byte destinationTone;
    public byte destinationNpi;

    //public byte interfaceVersion;
    public String bindOption;

    public int seqMod = 1;
    public int seqSeed = 0;

    @Bean
    public SmppProxyManager smppProxyManager() {
        SmppProxyManager smppProxyManager = new SmppProxyManager();

        try {
            smppProxyManager.addProxyFromConfig(this, new SmsReporterImpl(this));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //smppProxyManager.startAllProxies();

        return smppProxyManager;
    }
}
