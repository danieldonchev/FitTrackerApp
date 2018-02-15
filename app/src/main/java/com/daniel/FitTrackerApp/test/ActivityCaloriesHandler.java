package com.daniel.FitTrackerApp.test;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;


public class ActivityCaloriesHandler extends DefaultHandler {

    private Map<Float, Float> MET;
    private boolean stopParse, isActivityFound;
    private String tempVal;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equalsIgnoreCase("something")){

            stopParse = true;
            isActivityFound = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(isActivityFound){

        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }
}
