/**
 * This work is protected under copyrights held by the members of the
 * TOOP Project Consortium as indicated at
 * http://wiki.ds.unipi.gr/display/TOOP/Contributors
 * (c) 2018-2021. All rights reserved.
 *
 * This work is licensed under the EUPL 1.2.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *         https://joinup.ec.europa.eu/software/page/eupl
 */
package eu.toop.dsd.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.StreamHelper;

import eu.toop.edm.jaxb.dcatap.DCatAPDatasetType;

/**
 * A class to write DSD responses
 *
 * @author yerlibilgin
 */
public class DsdDataConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(DsdDataConverter.class);

  private static final Transformer transformer;

  static {
    try {
      LOGGER.info ("Starting to precompile DSD XSLT script");
      InputStream inputStream = DsdDataConverter.class.getResourceAsStream("/xslt/dsd.xslt");
      StreamSource stylesource = new StreamSource(inputStream);
      transformer = TransformerFactory.newInstance().newTransformer(stylesource);
      LOGGER.info ("Finsihed precompiling DSD XSLT script");

    } catch (TransformerConfigurationException e) {
      throw new DSDException("Cannot instantiate transformers");
    }
  }

  /**
   * Converts a DIR result to a DSD result
   *
   * @param directoryResult the xml received from the toop directory
   * @param datasetType     the optional datasetType parameter, used for filtering the record
   * @param dpType          the dpType query parameter for filtering and returning only the selected entities
   * @return
   */
  public static String convertDIRToDSDWithDPType(String directoryResult, String datasetType, String dpType) throws TransformerException {
    return convertDIRToDSD(directoryResult, datasetType, null, dpType);
  }

  /**
   * Converts a DIR result to a DSD result
   *
   * @param directoryResult the xml received from the toop directory
   * @param datasetType     the optional datasetType parameter, used for filtering the record
   * @param countryCode     the country code for filtering and returning only the selected countries
   * @return
   */
  public static String convertDIRToDSDWithCountryCode(String directoryResult, String datasetType, String countryCode) throws TransformerException {
    return convertDIRToDSD(directoryResult, datasetType, countryCode, null);
  }

  private static String convertDIRToDSD(String directoryResult, String datasetType, String countryCode, String dpType) throws TransformerException {
    transformer.clearParameters();
    if (datasetType != null)
      transformer.setParameter("datasetType", datasetType);

    if (countryCode != null)
      transformer.setParameter("countryCode", countryCode);

    if (dpType != null)
      transformer.setParameter("dpType", dpType);

    StringWriter writer = new StringWriter();
    StreamSource xmlSource = new StreamSource(new ByteArrayInputStream(directoryResult.getBytes(StandardCharsets.UTF_8)));
    transformer.transform(xmlSource, new StreamResult(writer));

    return writer.toString();
  }

  /**
   * Read the dsdRawResult as a List of {@link DCatAPDatasetType} objects
   *
   * @param dsdRawResult the raw DSD query result
   * @return the resulting list
   */
  public static List<DCatAPDatasetType> parseDataset(String dsdRawResult) {
    final InputStream inputStream = new ByteArrayInputStream(dsdRawResult.getBytes(StandardCharsets.UTF_8));
    return DcatDatasetTypeReader.parseDataset(new StreamSource(inputStream));
  }

  /**
   * Try a dummy transformation on the XSLT to make it ready for
   * future calls
   */
  public static void prepare() {
    new Thread(() -> {
      LOGGER.info("Starting Dummy prepare");
      try {
        final byte[] allBytes = StreamHelper.getAllBytes(DsdDataConverter.class.getResourceAsStream("/dummybusinesscard.xml"));
        String directoryResult = new String(allBytes, StandardCharsets.UTF_8);
        final String dcat = DsdDataConverter.convertDIRToDSDWithCountryCode(directoryResult, "FINANCIAL_RECORD_TYPE", "SV");
        LOGGER.debug(dcat);
      } catch (Exception ex) {
        LOGGER.warn("Couldn't execute transformation at startup" + ex.getMessage());
      } finally {
        LOGGER.info("Finished dummy prepare");
      }
    }).start();
  }
}
