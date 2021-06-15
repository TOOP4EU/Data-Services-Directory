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
package eu.toop.dsd.api.types;

import com.helger.commons.ValueEnforcer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides methods to resolve a DSD query checking
 * the requirements refined in the data-services-directory specification
 */
public class DSDQuery {
  /**
   * This enum represents the mandatory queryId parameter
   */
  public enum DSDQueryID {
    /**
     * Query by datasettype and dptype dsd query id.
     */
    QUERY_BY_DATASETTYPE_AND_DPTYPE("urn:toop:dsd:ebxml-regrem:queries:ByDatasetTypeAndDPType"),
    /**
     * Query by datasettype and location dsd query id.
     */
    QUERY_BY_DATASETTYPE_AND_LOCATION("urn:toop:dsd:ebxml-regrem:queries:ByDatasetTypeAndLocation");

    /**
     * The Id.
     */
    public final String id;

    DSDQueryID(String id) {
      this.id = id;
    }

    /**
     * Gets by id.
     *
     * @param queryId the query id
     * @return the by id
     */
    public static DSDQueryID getById(String queryId) {
      ValueEnforcer.notEmpty(queryId, "queryId");
      for (DSDQueryID dsdQueryID : DSDQueryID.values()) {
        if (queryId.equals(dsdQueryID.id))
          return dsdQueryID;
      }

      throw new IllegalArgumentException("Invalid queryId [" + queryId + "]");
    }
  }

  /**
   * DatasetType parameter, Must have the value
   * <code>urn:toop:dsd:ebxml-regrem:queries:ByDatasetTypeAndDPType</code>
   * for <code>QUERY_BY_DATASETTYPE_AND_DPTYPE</code>,<br><br>
   * and <code>urn:toop:dsd:ebxml-regrem:queries:ByDatasetTypeAndLocation</code>
   * for <code>QUERY_BY_DATASETTYPE_AND_LOCATION</code>
   */
  public static final String PARAM_NAME_DATA_SET_TYPE = "dataSetType";
  /**
   * The constant PARAM_NAME_QUERY_ID.
   */
  public static final String PARAM_NAME_QUERY_ID = "queryId";
  /**
   * The constant PARAM_NAME_DATA_PROVIDER_TYPE.
   */
  public static final String PARAM_NAME_DATA_PROVIDER_TYPE = "dataProviderType";
  /**
   * The constant PARAM_NAME_COUNTRY_CODE.
   */
  public static final String PARAM_NAME_COUNTRY_CODE = "countryCode";


  private final DSDQueryID queryId;
  private final Map<String, String> parameters;


  private DSDQuery(DSDQueryID queryId, Map<String, String> parameters) {
    this.queryId = queryId;
    this.parameters = parameters;
  }

  /**
   * Resolve dsd query.
   *
   * @param parameterMap the parameter map
   * @return the dsd query
   */
  public static DSDQuery resolve(Map<String, String[]> parameterMap) {

    String[] queryId = parameterMap.get(PARAM_NAME_QUERY_ID);
    ValueEnforcer.notEmpty(queryId, "queryId");
    if (queryId.length != 1)
      throw new IllegalStateException("queryId invalid");

    DSDQueryID queryID = DSDQueryID.getById(queryId[0]);

    return verifyAndResolve(queryID, parameterMap);
  }

  private static DSDQuery verifyAndResolve(DSDQueryID queryID, Map<String, String[]> parameterMap) {
    boolean valid = false;
    switch (queryID) {
      case QUERY_BY_DATASETTYPE_AND_DPTYPE: {
        valid = parameterMap.containsKey(PARAM_NAME_QUERY_ID) &&
            parameterMap.containsKey(PARAM_NAME_DATA_SET_TYPE) &&
            parameterMap.containsKey(PARAM_NAME_DATA_PROVIDER_TYPE);


      }
      break;
      case QUERY_BY_DATASETTYPE_AND_LOCATION: {
        valid = parameterMap.containsKey(PARAM_NAME_QUERY_ID) &&
            parameterMap.containsKey(PARAM_NAME_DATA_SET_TYPE);
        // that one is optional && parameterMap.containsKey(PARAM_NAME_COUNTRY_CODE);
      }
    }

    if (!valid)
      throw new IllegalArgumentException("Invalid paramater map");

    Map<String, String> newParamMap = new HashMap<>();
    parameterMap.forEach((k, v) -> {
      if (v.length != 1)
        throw new IllegalArgumentException("Invalid Query");

      newParamMap.put(k, v[0]);
    });

    return new DSDQuery(queryID, newParamMap);
  }

  /**
   * Get the value of the given parameter
   *
   * @param parameterName the parameter
   * @return parameter value (null if doesn't exist)
   */
  public @Nullable
  String getParameterValue(@Nonnull String parameterName) {
    ValueEnforcer.notEmpty(parameterName, "parameterName");
    return parameters.get(parameterName);

  }


  /**
   * Get the value of the given parameter
   *
   * @param parameterName the parameter
   * @return parameter value
   */
  public @Nonnull
  String safeGetParameterValue(@Nonnull String parameterName) {
    ValueEnforcer.notEmpty(parameterName, "parameterName");
    
    if (parameters.containsKey(parameterName))
      return parameters.get(parameterName);

    throw new IllegalArgumentException("No parameter value for " + parameterName);
  }

  /**
   * create and return an iterator for all params
   *
   * @return the iterator
   */
  public Iterator<Map.Entry<String, String>> getAllParameters() {
    return parameters.entrySet().iterator();
  }

  /**
   * Gets query id.
   *
   * @return the query id
   */
  public DSDQueryID getQueryId() {
    return queryId;
  }
}
