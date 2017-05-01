
package org.elasticsearch.plugin.kbrdemo.utils;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.plugin.kbrdemo.validation.ValidatorsRepo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class SettingsHandler {
  private static final Logger logger = Loggers.getLogger(SettingsHandler.class);

  private static SettingsHandler currentSettingsInstance;
  private final Client client;
  public Settings settings;

  public KerbUtil kerbUtil = new KerbUtil();

  public ValidatorsRepo validatorsRepo;
  public CompletableFuture<Integer> future;

  @Inject
  public SettingsHandler(Settings settings, Client client) {
    this.client = client;
    this.settings = settings;
    readSettings(settings);

    logger.info("setting begin .......");
    logger.info(settings.toDelimitedString(';'));
    logger.info("setting end .......");

    //System.setProperty( "sun.security.krb5.debug", "true");
    //System.setProperty( "java.security.auth.login.config", "/Users/yilong/setup/elasticsearch-5.3.0/plugins/kbrdemo/jaas.conf");
    //System.setProperty( "javax.security.auth.useSubjectCredsOnly", "true");

    // Try to fetch from
    if (client != null) {
      try {
        updateSettingsFromIndex(client);
      } catch (IllegalStateException ise) {
        // Not ready yet.
        return;
      } catch (Exception e) {
        e.printStackTrace();
        logger.info("No cluster-wide settings found.. You need RedonlyREST Kibana plugin to make this work :) ");
      }
    }

  }

  public static SettingsHandler getInstance(Settings s, Client c) {
    if (currentSettingsInstance == null) {
      currentSettingsInstance = new SettingsHandler(s, c);
    }
    return currentSettingsInstance;
  }

  private static Setting<String> str(String name) {
    return new Setting<>(name, "", (value) -> value, Setting.Property.NodeScope);
  }

  private static Setting<Boolean> bool(String name) {
    return Setting.boolSetting(name, Boolean.FALSE, Setting.Property.NodeScope);
  }

  //  private static Setting<Integer> integ(String name) {
//    return Setting.intSetting(name, 0, Integer.MAX_VALUE, Setting.Property.NodeScope);
//  }
  private static Setting<Settings> grp(String name) {
    return Setting.groupSetting(name, new Setting.Property[]{Setting.Property.Dynamic, Setting.Property.NodeScope});
  }

//  private static Setting<List<String>> strA(String name) {
//    return Setting.listSetting(name, new ArrayList<>(), (s) -> s.toString(), Setting.Property.NodeScope);
//  }

  public static List<Setting<?>> allowedSettings() {
    String prefix = "kbrdemo.";
    return Arrays.asList(
            str(prefix + "kerb.jaas_path"),
            str(prefix + "kerb.type"),
            str(prefix + "kerb.keytab_path"),
            str(prefix + "kerb.principal"),
            str(prefix + "kerb.file_path"),
            str(prefix + "kerb.roles")
    );
  }

  public void updateSettingsFromIndex(Client client) throws ResourceNotFoundException {
    GetResponse resp = client.prepareGet(".kbrdemo", "settings", "1").get();
    if (!resp.isExists()) {
      logger.error(" there is no settings found ... ");
      //throw new ElasticsearchException("no settings found in index");
      validatorsRepo = new ValidatorsRepo(null, kerbUtil);
    } else {
      String yaml = (String) resp.getSource().get("settings");
      Settings settings = Settings.builder().loadFromSource(yaml, XContentType.YAML).build();
      readSettings(settings);
    }

    currentSettingsInstance = this;
  }

  private void readSettings(Settings settings) {
      this.settings = settings;

    Settings s = settings.getByPrefix("kbrdemo.");
    if (s == null) {
        logger.info(">>>>>>>>>>>>>> kbrdemo setting error <<<<<<<<<<<<<<< " );
        return;
    }

    kerbUtil.jaasPath = s.get("kerb.jaas_path", null);
    kerbUtil.kerbType = s.get("kerb.type", "kerberos");
    kerbUtil.keytabPath = s.get("kerb.keytab_path", null);
    kerbUtil.kerbPrincipal = s.get("kerb.principal", null);
    kerbUtil.krb5FilePath = s.get("krb5.file_path", null);
    kerbUtil.kerbRoles = s.get("kerb.roles", null);

    logger.info("setting of kerb is : " + kerbUtil.toString());

    validatorsRepo = new ValidatorsRepo(settings, kerbUtil);

    currentSettingsInstance = this;
  }
}
