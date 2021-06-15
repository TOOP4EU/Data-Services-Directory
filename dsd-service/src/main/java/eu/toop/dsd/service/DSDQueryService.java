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
package eu.toop.dsd.service;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import eu.toop.dsd.api.DsdDataConverter;
import eu.toop.dsd.api.ToopDirClient;
import eu.toop.dsd.config.DSDConfig;
import eu.toop.dsd.api.types.DSDQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * This class is the main query processor for the DSD queries. It processes
 * the incoming queries and generates the required results with
 * respect to the TOOP DSD specifications.
 *
 * @author yerlibilgin
 */
public class DSDQueryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DSDQueryService.class);

  /**
   * Query the underlying database for the provided parameters and
   * respond using the TOOP DSD RegRep response specification.
   *
   * @param parameterMap   the map that contains the parameters for the queries, may not be null
   * @param responseStream the stream to write the results in case of success, may not be null
   * @throws IllegalArgumentException if the query parameters are invalid
   * @throws IllegalStateException    if a problem occurs
   */
  public static void processRequest(@Nonnull @Nonempty Map<String, String[]> parameterMap, @Nonnull OutputStream responseStream) throws IOException, TransformerException {
    ValueEnforcer.notNull(parameterMap, "parameterMap");
    ValueEnforcer.notNull(responseStream, "responseStream");

    if (parameterMap.isEmpty())
      throw new IllegalArgumentException("parameterMap cannot be empty");


    final DSDQuery dsdQuery = DSDQuery.resolve(parameterMap);

    //currently only one type of query is supported
    switch (dsdQuery.getQueryId()) {
      case QUERY_BY_DATASETTYPE_AND_DPTYPE: {
        processDataSetRequestByDPType(dsdQuery, responseStream);
        break;
      }

      case QUERY_BY_DATASETTYPE_AND_LOCATION: {
        processDataSetRequestByLocation(dsdQuery, responseStream);
        break;
      }
    }
  }

  /**
   * Processes the incoming parameter map as a dataset request parameter map and performs a dataset request with respect to
   * <code>urn:toop:dsd:ebxml-regrem:queries:ByDatasetTypeAndDPType</code>
   *
   * @param dsdQuery       the parameters resolved as a {@link DSDQuery} object
   * @param responseStream the result will be written into this stream
   * @throws IOException if an io problem occurs.
   */
  public static void processDataSetRequestByDPType(DSDQuery dsdQuery, OutputStream responseStream) throws IOException, TransformerException {
    ValueEnforcer.notNull(dsdQuery, "dsdQuery");
    ValueEnforcer.notNull(responseStream, "responseStream");


    String dataSetType = dsdQuery.safeGetParameterValue(DSDQuery.PARAM_NAME_DATA_SET_TYPE);
    String dpType = dsdQuery.safeGetParameterValue(DSDQuery.PARAM_NAME_DATA_PROVIDER_TYPE);

    LOGGER.debug("Processing data set request [dataSetType: " + dataSetType + ", dpType: " + dpType + "]");

    //query all the matches without a document type id.
    final String directoryResult = ToopDirClient.callSearchApiForDpType(DSDConfig.getToopDirUrl(), dpType);

    String resultXml = DsdDataConverter.convertDIRToDSDWithDPType(directoryResult, dataSetType, dpType);

    responseStream.write(resultXml.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Processes the incoming parameter map as a dataset request parameter map and performs a dataset request with respect to
   * <code>urn:toop:dsd:ebxml-regrem:queries:ByDatasetTypeAndLocation</code>
   *
   * @param dsdQuery       the parameters resolved as a {@link DSDQuery} object
   * @param responseStream the result will be written into this stream
   * @throws IOException if an io problem occurs.
   */
  public static void processDataSetRequestByLocation(@Nonnull DSDQuery dsdQuery, @Nonnull OutputStream responseStream) throws IOException, TransformerException {
    ValueEnforcer.notNull(dsdQuery, "dsdQuery");
    ValueEnforcer.notNull(responseStream, "responseStream");


    String dataSetType = dsdQuery.safeGetParameterValue(DSDQuery.PARAM_NAME_DATA_SET_TYPE);
    String countryCode = dsdQuery.safeGetParameterValue(DSDQuery.PARAM_NAME_COUNTRY_CODE);

    LOGGER.debug("Processing data set request [dataSetType: " + dataSetType +
        ", countryCode: " + countryCode + "]");

    //query all the matches without a document type id.
    final String directoryResult = ToopDirClient.callSearchApiWithCountryCode(DSDConfig.getToopDirUrl(), countryCode);

    String resultXml = DsdDataConverter.convertDIRToDSDWithCountryCode(directoryResult, dataSetType, countryCode);

    responseStream.write(resultXml.getBytes(StandardCharsets.UTF_8));
  }
}
