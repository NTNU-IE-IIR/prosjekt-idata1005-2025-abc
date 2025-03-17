package utils;

import java.util.prefs.Preferences;

public class PreferenceUtil {
  private final Preferences prefs;

  public PreferenceUtil(String baseClass ) {
    prefs = Preferences.userRoot().node(baseClass);
  }

  public void saveSetting(String key, String value) {
    prefs.put(key, value);
  }

  public String getSetting(String key, String defaultValue) {
    return prefs.get(key, defaultValue);
  }

  public String getSetting(String key)  {
    return prefs.get(key, "Could not retrieve.");
  }

}
