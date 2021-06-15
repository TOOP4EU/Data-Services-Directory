/**
 * Copyright 2021 - TOOP Project
 *
 * This file and its contents are licensed under the EUPL, Version 1.2
 * or – as soon they will be approved by the European Commission – subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *       https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.toop.dsd.api;

import org.junit.Test;

import java.io.IOException;

public class ToopDirClientTest {

  public static final String TOOP_DIR_URL = "http://directory.acc.exchange.toop.eu";

  @Test
  public void searchCountry() throws IOException {
    final String result = ToopDirClient.callSearchApiWithCountryCode(TOOP_DIR_URL, "SV");
    System.out.println(result);
  }

  @Test
  public void searchDpType() throws IOException {
    final String result = ToopDirClient.callSearchApiForDpType(TOOP_DIR_URL, "abc");
    System.out.println(result);
  }
}