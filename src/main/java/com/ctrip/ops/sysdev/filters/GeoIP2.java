package com.ctrip.ops.sysdev.filters;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.maxmind.geoip2.record.Subdivision;
import org.apache.log4j.Logger;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;

public class GeoIP2 extends BaseFilter {
    private static final Logger logger = Logger.getLogger(GeoIP2.class
            .getName());

    public GeoIP2(Map config) {
        super(config);
    }

    private String source;
    private String target;
    private DatabaseReader reader;

    protected void prepare() {
        if (!config.containsKey("source")) {
            logger.error("no source configured in GeoIP");
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
            logger.error("no database configured in GeoIP");
            System.exit(1);
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
                logger.error("failed to load " + databasePath);
                System.exit(1);
            }
            // This creates the DatabaseReader object, which should be reused across lookups.
            try {
                this.reader = new DatabaseReader.Builder(connection.getInputStream()).build();
            } catch (IOException e) {
                logger.error("failed to prepare DatabaseReader for geoip");
                logger.error(e);
                System.exit(1);
            }

        } else {
            File database = new File(databasePath);
            // This creates the DatabaseReader object, which should be reused across lookups.
            try {
                this.reader = new DatabaseReader.Builder(database).build();
            } catch (IOException e) {
                logger.error("failed to prepare DatabaseReader for geoip");
                logger.error(e);
                System.exit(1);
            }
        }
    }

    ;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Map filter(final Map event) {
        if (event.containsKey(this.source)) {

            boolean success = true;

            InetAddress ipAddress;
            try {
                ipAddress = InetAddress.getByName((String) event.get(source));

                CityResponse response = reader.city(ipAddress);

                Country country = response.getCountry();
                Subdivision subdivision =
                        response.getMostSpecificSubdivision();
                City city = response.getCity();
                Location location = response.getLocation();

                Map targetObj = new HashMap();
                event.put(this.target, targetObj);
                targetObj.put("country_code", country.getIsoCode());
                targetObj.put("country_name", country.getName());
                targetObj.put("subdivision_name", subdivision.getName());
                targetObj.put("country_isocode", country.getIsoCode());
                targetObj.put("city_name", city.getName());
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                targetObj.put("latitude", latitude);
                targetObj.put("longitude", longitude);
                targetObj.put("location", new double[]{longitude, latitude});
            } catch (Exception e) {
                logger.debug(e);
                success = false;
            }

            this.postProcess(event, success);
        }

        return event;
    }
}
