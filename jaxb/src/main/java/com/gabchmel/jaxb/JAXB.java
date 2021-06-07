package com.gabchmel.jaxb;

import org.dmg.pmml.PMML;
import org.jpmml.model.PMMLUtil;
import org.jpmml.model.SerializationUtil;
import org.jpmml.model.visitors.LocatorNullifier;
import org.jpmml.model.visitors.StringInterner;
import org.pmml4s.model.Model;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;


public class JAXB {

    private final static String BASE_PATH = "c:\\src\\ContextPlayerPython\\";

    public static void main(String[] args) throws Exception {
        run();
    }

    public static void run () throws JAXBException, SAXException {
//        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

        File pmmlFile = new File(BASE_PATH + "decision_tree.pmml");
        File serFile = new File(BASE_PATH + "model.pmml.ser");

        // Parse a pmml object from a file
        PMML pmml = null;
        try (InputStream is = new FileInputStream(pmmlFile)) {
//            Source source = ImportFilter.apply(new InputSource(is));
        pmml = PMMLUtil.unmarshal(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Apply a visitor
        StringInterner visitor = new StringInterner();
        visitor.applyTo(pmml);

        LocatorNullifier locatorNullifier = new LocatorNullifier();
        locatorNullifier.applyTo(pmml);

        // Write an ser file from the pmml object
        try (OutputStream os = new FileOutputStream(serFile)) {
            SerializationUtil.serializePMML(pmml, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        float[] data = {0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f};

        Model model = Model.fromFile("c:\\src\\ContextPlayerPython\\model.pmml.ser");
        Object result = model.predict(data);
        System.out.print(result);
    }
}