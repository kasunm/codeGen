package com.gmail.kasun.codegen.util;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class TestDataHelper {
    private final static TestDataHelper instance = new TestDataHelper();
    private Properties prop = new Properties();
    private String [] maleNames;
    private String [] feMaleNames;
    private String [] emailDomains;
    private String [] itemNames;
    private String  description;

    private TestDataHelper(){
        try {
            InputStream input = new FileInputStream("templates/partial/testData.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        maleNames = prop.getProperty("male.names").split(",");
        feMaleNames = prop.getProperty("female.names").split(",");
        emailDomains = prop.getProperty("emailDomains").split(",");
        itemNames = prop.getProperty("items").split(",");
        description = prop.getProperty("description");
    }

    public static TestDataHelper getInstance() {
        return instance;
    }

    public String getRandomMaleName(){
        return maleNames[(int)(Math.random() * (maleNames.length -1))];
    }

    public String getRandomFemaleName(){
        return feMaleNames[(int)(Math.random() * (feMaleNames.length -1))];
    }

    public String getRandomName(){
        if(Math.random() < 0.5) return getRandomFemaleName();
        else return getRandomMaleName();
    }

    public String getRandomName(Integer minLength, Integer maxLength){
        if(maxLength == null) maxLength = 250;
        String value = getRandomName();
        if(minLength != null && minLength > 0 && value.length() < minLength) return StringUtils.rightPad(value, minLength + 1, 'a');
        if(maxLength != null && maxLength > 0 && value.length() > maxLength ) return value.substring(0, maxLength);
        return value;
    }

    public String getRandomEmail(Integer minLength, Integer maxLength){
        if(maxLength == null) maxLength = 250;
        String value = getRandomName() + emailDomains[(int)(Math.random() * (emailDomains.length -1))] ;
        if(minLength != null && minLength > 0 && value.length() < minLength) return StringUtils.rightPad(value, minLength + 1, 'a');
        if(maxLength != null && maxLength > 0 && value.length() > maxLength ) return value.substring(0, maxLength);
        return value;
    }

    public String getRandomItemName(Integer minLength, Integer maxLength){
        if(maxLength == null) maxLength = 250;
        String value =  itemNames[(int)(Math.random() * (itemNames.length -1))];
        if(minLength != null && minLength > 0 && value.length() < minLength) return StringUtils.rightPad(value, minLength + 1, 'a');
        if(maxLength != null && maxLength > 0 && value.length() > maxLength ) return value.substring(0, maxLength);
        return value;
    }

    public String getRandomDescription(Integer min, Integer max){
        if(min == null) min = 0;
        if(max == null) max = 250;
        int maxPossible = description.length();
        if(maxPossible > max) maxPossible = max;
        int length = min + (int) (Math.random() * (maxPossible - min));
        if(length == 0) length = 1;
        int start = (int) (Math.random() * ( maxPossible - length));
        return description.substring(start, start + length);
    }

    /**
     * Will return string with a character and set of numers . e, V456565656
     * @param min
     * @param max
     * @return String
     */
    public String getRandomNumberStringWitLength(Integer min, Integer max){
        if(min == null) min = 1;
        if(max == null) max = 250;
        StringBuilder sb = new StringBuilder(max + 1);
        Random r = new Random();
        char c = (char)(r.nextInt(26) + 'a');
        sb.append(c);
        int dataLength = min + (int)(Math.random() * (max - min));
        for(int i = 1; i < dataLength; i++){
            int randomNumber = (int)(Math.random() * 9);
            sb.append(randomNumber);
        }
        return  sb.toString();
    }

    public String getDescription(int length){
        return getRandomDescription(length, length);
    }

    public Integer getRandomIntegerValue(Integer min, Integer max){
        if(min == null) min = 0;
        if(max == null) max = 999999;
        return Integer.valueOf( min + (int)(Math.random() * (max - min)));
    }

    public Long getRandomLongValue(Integer min, Integer max){
        if(min == null) min = 0;
        if(max == null) max = 999999;
        return Long.valueOf( min.longValue() + (long)(Math.random() * (max.longValue() - min.longValue())));
    }

    public Double getRandomDoubleValue(Integer min, Integer max){
        if(min == null) min = 0;
        if(max == null) max = 999999;
        return Double.valueOf( min + (Math.random() * (max - min)));
    }

    public String getRandomLocalDateTime(){
        return "LocalDateTime.of(2000 + (int)(Math.random() * 19), 1 + (int)(Math.random() * 11), 1 + (int)(Math.random() * 27), (int)(Math.random() * 23),(int)(Math.random() * 59),(int)(Math.random() * 59),0)";
    }

    public String getRandomLocalDate(){
        return "LocalDate.of(2000 + (int)(Math.random() * 19), 1 +  (int)(Math.random() * 11), 1 + (int)(Math.random() * 27))";
    }

    public String getRandomLocalTime(){
        return "LocalTime.of((int)(Math.random() * 23),(int)(Math.random() * 59),(int)(Math.random() * 59),0)";
    }






}
