/*
 * Copyright 2007-2015 The Europeana Foundation
 * 
 * Licensed under the EUPL, Version 1.1 (the "License") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the License.
 * 
 * You may obtain a copy of the License at: http://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.europeana.uim.repoxclient.rest;

import java.util.List;

import javax.ws.rs.InternalServerErrorException;

import pt.utl.ist.dataProvider.Aggregator;
import pt.utl.ist.dataProvider.DataProvider;
import pt.utl.ist.util.ProviderType;
import pt.utl.ist.util.exceptions.AlreadyExistsException;
import pt.utl.ist.util.exceptions.DoesNotExistException;
import pt.utl.ist.util.exceptions.InvalidArgumentsException;
import pt.utl.ist.util.exceptions.MissingArgumentsException;
import eu.europeana.uim.Registry;
import eu.europeana.uim.repox.model.RepoxConnectionStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@theeuropeanlibrary.org)
 * @since Apr 29, 2015
 */
public interface RepoxUIMServiceT {

  /**
   * Get the connection status URL
   * 
   * @return RepoxConnectionStatus
   */
  RepoxConnectionStatus showConnectionStatus();

  /******************** Aggregator Calls ********************/
  /**
   * Create an aggregator.
   * 
   * @param id
   * @param name
   * @param nameCode
   * @param homepage
   * @throws InvalidArgumentsException
   * @throws MissingArgumentsException
   * @throws AlreadyExistsException
   * @throws InternalServerErrorException
   */
  void createAggregator(String id, String name, String nameCode, String homepage)
      throws InvalidArgumentsException, MissingArgumentsException, AlreadyExistsException,
      InternalServerErrorException;

  /**
   * Delete an aggregator by specifying the Id.
   * 
   * @param aggregatorId
   * @throws DoesNotExistException
   * @throws InternalServerErrorException
   */
  void deleteAggregator(String aggregatorId) throws DoesNotExistException,
      InternalServerErrorException;

  /**
   * Retrieve the aggregator with the provided id.
   * 
   * @param aggregatorId
   * @return boolean value
   */
  boolean aggregatorExists(String aggregatorId);

  /**
   * Update an aggregator by specifying the Id. Aggregator newId can be null if there is no need to
   * change the id.
   * 
   * @param id
   * @param newId
   * @param name
   * @param nameCode
   * @param homepage
   * @throws InvalidArgumentsException
   * @throws MissingArgumentsException
   * @throws DoesNotExistException
   * @throws InternalServerErrorException
   */
  void updateAggregator(String id, String newId, String name, String nameCode, String homepage)
      throws InvalidArgumentsException, MissingArgumentsException, DoesNotExistException,
      InternalServerErrorException;

  /**
   * Get a list of aggregators in the specified range. Returned number can be smaller than the
   * requested. Offset not allowed negative. If number is negative then it returns all the items
   * from offset until the total number of items.
   * 
   * @param offset
   * @param number
   * @return
   * @throws InvalidArgumentsException
   */
  List<Aggregator> getAggregatorList(int offset, int number) throws InvalidArgumentsException;

  /******************** Provider Calls ********************/

  /**
   * Retrieve the provider with the provided id.
   * 
   * @param id
   * @return boolean value
   */
  boolean providerExists(String id);

  /**
   * Get a list of provider in the specified range. Returned number can be smaller than the
   * requested. Offset not allowed negative. If number is negative then it returns all the items
   * from offset until the total number of items.
   * 
   * @param aggregatorId
   * @param offset
   * @param number
   * @return
   * @throws InvalidArgumentsException
   * @throws DoesNotExistException
   */
  List<DataProvider> getProviderList(String aggregatorId, int offset, int number)
      throws InvalidArgumentsException, DoesNotExistException;

  /**
   * Create a provider.
   * 
   * @param aggregatorId
   * @param id
   * @param name
   * @param country
   * @param description
   * @param nameCode
   * @param homepage
   * @param providerType
   * @param email
   * @throws InvalidArgumentsException
   * @throws MissingArgumentsException
   * @throws AlreadyExistsException
   * @throws InternalServerErrorException
   * @throws DoesNotExistException
   */
  void createProvider(String aggregatorId, String id, String name, String country,
      String description, String nameCode, String homepage, ProviderType providerType, String email)
      throws InvalidArgumentsException, MissingArgumentsException, AlreadyExistsException,
      InternalServerErrorException, DoesNotExistException;

  /**
   * Delete an provider by specifying the Id.
   * 
   * @param providerId
   * @throws DoesNotExistException
   * @throws InternalServerErrorException
   */
  void deleteProvider(String providerId) throws DoesNotExistException, InternalServerErrorException;

  /**
   * Update a provider by specifying the Id. Provider newId can be null if there is no need to
   * change the id.
   * 
   * @param id
   * @param newId
   * @param newAggregatorId
   * @param name
   * @param country
   * @param description
   * @param nameCode
   * @param homepage
   * @param providerType
   * @param email
   * @throws InvalidArgumentsException
   * @throws DoesNotExistException
   * @throws MissingArgumentsException
   * @throws AlreadyExistsException
   */
  void updateProvider(String id, String newId, String newAggregatorId, String name, String country,
      String description, String nameCode, String homepage, ProviderType providerType, String email)
      throws InvalidArgumentsException, DoesNotExistException, MissingArgumentsException,
      AlreadyExistsException;
  
  public void setRegistry(Registry registry);

  public Registry getRegistry();
}
