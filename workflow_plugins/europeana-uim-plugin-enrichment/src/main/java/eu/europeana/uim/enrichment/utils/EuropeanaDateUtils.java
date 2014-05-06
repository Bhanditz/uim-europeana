/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.uim.enrichment.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.solr.entity.ProxyImpl;

/**
 * String heuristics for date normalization
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class EuropeanaDateUtils {

    public static String bcList;
    public static String[] bc;

    public static void setPath(String path) {
        PropertyReader.loadPropertiesFromFile(path);
    }

    public List<String> createEuropeanaYears(ProxyImpl proxy) {
        List<String> years = new ArrayList<String>();
        try {

            if (bcList == null) {
                bcList = FileUtils
                        .readFileToString(
                                new File(
                                        PropertyReader
                                        .getProperty(UimConfigurationProperty.UIM_BCLIST_PATH)),
                                "UTF-8");
                bc = StringUtils.split(bcList, "\n");
            }

            if (proxy.getDcDate() != null) {
                for (Entry<String, List<String>> entry : proxy.getDcDate().entrySet()) {
                    for (String date : entry.getValue()) {

                        if (!years.contains(date)) {
                            years.add(date);
                        }

                    }
                }
            }

            if (proxy.getDctermsTemporal() != null) {
                for (Entry<String, List<String>> entry : proxy.getDctermsTemporal().entrySet()) {
                    for (String date : entry.getValue()) {
                        if (!years.contains(date)) {
                            years.add(date);
                        }
                    }
                }
            }

            if (proxy.getDctermsCreated() != null) {
                for (Entry<String, List<String>> entry : proxy.getDctermsCreated().entrySet()) {
                    for (String date : entry.getValue()) {
                        if (!years.contains(date)) {
                            years.add(date);
                        }
                    }
                }
            }

            if (proxy.getDctermsIssued() != null) {
                for (Entry<String, List<String>> entry : proxy.getDctermsIssued().entrySet()) {
                    for (String date : entry.getValue()) {
                        if (!years.contains(date)) {
                            years.add(date);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return years;
    }

    private List<String> refineDates(String[] patterns, String input) {
        List<String> dates = new ArrayList<String>();
        String contains = contains(input, patterns);
        if (contains != null) {
            dates.addAll(getDates(input, contains));

        } else if (isSingleDate(input, 10)) {
            if (!dates.contains(input)) {
                dates.add(input);
            }
        }
        return dates;
    }

    private boolean isSingleDate(String input, int radix) {
        if (input.isEmpty()) {
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            if (i == 0 && input.charAt(i) == '-') {
                if (input.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if (Character.digit(input.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }

    private String contains(String input, String[] filters) {
        if (filters != null) {
            for (String filter : filters) {
                if ((StringUtils.containsIgnoreCase(input, " " + filter + " ") || StringUtils.endsWithIgnoreCase(input.
                        trim(), " " + filter)) && !isUri(input)) {
                    return filter;
                }
            }
        }
        return null;
    }

    private boolean isUri(String input) {
        try {
            new URL(input);
            return true;
        } catch (Exception e) {
            //do nothing
        }
        return false;
    }

    // TODO: not used for the time being as it created problems
    private List<String> clean(String inputField) {
        List<String> dates = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        char[] chars = inputField.toCharArray();
        int i = 0;
        for (char ch : chars) {
            if (Character.isDigit(ch)) {
                sb.append(ch);

                if (i == chars.length - 1 || !Character.isDigit(chars[i + 1])) {
                    dates.add(sb.toString());
                    sb = new StringBuffer();
                }
            }
            i++;
        }
        return dates;
    }

    private List<String> getDates(String input, String remove) {
        List<String> dates = new ArrayList<String>();
        String[] left = StringUtils.splitByWholeSeparator(input.toLowerCase(), remove.toLowerCase());
        List<Character> chars = new ArrayList<Character>();
        boolean hasDigits = false;
        boolean foundNum = false;
        for (String str : left) {
            if (str.length() > 0) {
                chars.add('-');
                for (char ch : str.toCharArray()) {
                    if (Character.isDigit(ch) && !foundNum) {
                        chars.add(ch);
                        hasDigits = true;
                    } else if (foundNum && Character.isDigit(ch)) {
                        chars.clear();
                    } else if (hasDigits) {
                        foundNum = true;
                    }
                }
                chars.add(',');
            }
        }
        StringBuffer sb = new StringBuffer();
        for (char ch : chars) {
            if (ch != ',') {
                sb.append(ch);
            } else {
                if (sb.length() > 0) {
                    dates.add(sb.toString());
                    sb = new StringBuffer();
                }
            }
        }
        return dates;
    }
}
