package com.ctrip.ops.sysdev.filters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Subdivision;
import lombok.extern.log4j.Log4j2;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;

@Log4j2
public class GeoIP2 extends BaseFilter {

    public GeoIP2(Map config) {
        super(config);
    }

    private String source;
    private String target;
    private DatabaseReader reader;
    private boolean country_code, country_name, country_isocode, subdivision_name, city_name, latitude, longitude, location;

    protected void prepare() {
        if (!config.containsKey("source")) {
            log.error("no source configured in GeoIP");
            System.exit(1);
        }
        this.source = (String) config.get("source");

        if (config.containsKey("target")) {
            this.target = (String) config.get("target");
        } else {
            this.target = "geoip";
        }

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "geoipfail";
        }

        // A File object pointing to your GeoIP2 or GeoLite2 database
        if (!config.containsKey("database")) {
            log.error("no database configured in GeoIP");
            System.exit(1);
        }

        if (config.containsKey("country_code")) {
            this.country_code = (Boolean) config.get("country_code");
        } else {
            this.country_code = true;
        }

        for (String fieldname : Arrays.asList("country_code", "country_name", "subdivision_name", "country_isocode", "city_name", "latitude", "longitude", "location")) {
            try {
                Field f = this.getClass().getDeclaredField(fieldname);
                if (config.containsKey(fieldname)) {
                    f.set(this, (Boolean) config.get(fieldname));
                } else {
                    f.set(this, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("could not get/set " + fieldname + " as boolean value");
                System.exit(1);
            }
        }


        String databasePath = (String) config.get("database");
        if (databasePath.startsWith("http://") || databasePath.startsWith("https://")) {
            URL httpUrl;
            URLConnection connection = null;
            try {
                httpUrl = new URL(databasePath);
                connection = httpUrl.openConnection();
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("failed to load " + databasePath);
                System.exit(1);
            }
            // This creates the DatabaseReader object, which should be reused across lookups.
            try {
                this.reader = new DatabaseReader.Builder(connection.getInputStream()).build();
            } catch (IOException e) {
                log.error("failed to prepare DatabaseReader for geoip");
                log.error(e);
                System.exit(1);
            }

        } else {
            File database = new File(databasePath);
            // This creates the DatabaseReader object, which should be reused across lookups.
            try {
                this.reader = new DatabaseReader.Builder(database).build();
            } catch (IOException e) {
                log.error("failed to prepare DatabaseReader for geoip");
                log.error(e);
                System.exit(1);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Map filter(final Map event) {
        if (event.containsKey(this.source)) {

            InetAddress ipAddress;

            try {
                ipAddress = InetAddress.getByName((String) event.get(source));
            } catch (UnknownHostException e) {
                log.debug("NOT a valid IP address");
                this.postProcess(event, false);
                return event;
            }


            try {
                CityResponse response = reader.city(ipAddress);
                Country country = response.getCountry();
                Subdivision subdivision =
                        response.getMostSpecificSubdivision();
                City city = response.getCity();
                Location location = response.getLocation();

                Map targetObj = new HashMap();
                event.put(this.target, targetObj);
                if (this.country_code)
                    targetObj.put("country_code", country.getIsoCode());
                if (this.country_name)
                    targetObj.put("country_name", country.getName());
                if (this.subdivision_name)
                    targetObj.put("subdivision_name", subdivision.getName());
                if (this.country_isocode)
                    targetObj.put("country_isocode", country.getIsoCode());
                if (this.city_name)
                    targetObj.put("city_name", city.getName());
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (this.latitude)
                    targetObj.put("latitude", latitude);
                if (this.longitude)
                    targetObj.put("longitude", longitude);
                if (this.location)
                    targetObj.put("location", new double[]{longitude, latitude});

                this.postProcess(event, true);
                return event;
            } catch (Exception e) {
                log.debug(e);
                log.debug("maybe your DB doesn't support city lelel.");
            }


            try {
                CountryResponse response = reader.country(ipAddress);
                Country country = response.getCountry();

                Map targetObj = new HashMap();
                event.put(this.target, targetObj);
                if (this.country_code)
                    targetObj.put("country_code", country.getIsoCode());
                if (this.country_name)
                    targetObj.put("country_name", country.getName());
                if (this.country_isocode)
                    targetObj.put("country_isocode", country.getIsoCode());
                if (this.city_name) ;

            } catch (Exception e) {
                log.debug(e);
                this.postProcess(event, false);
                return event;
            }
        }

        return event;
    }
}
