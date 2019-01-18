package com.salesmanager.shop.store.api.v1.store;

import java.security.Principal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.entity.EntityExists;
import com.salesmanager.shop.model.shop.PersistableMerchantStore;
import com.salesmanager.shop.model.shop.ReadableMerchantStore;
import com.salesmanager.shop.model.shop.ReadableMerchantStoreList;
import com.salesmanager.shop.store.api.exception.UnauthorizedException;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.store.controller.user.facade.UserFacade;
import com.salesmanager.shop.utils.LanguageUtils;
import com.salesmanager.shop.utils.ServiceRequestCriteriaBuilderUtils;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/v1")
public class MerchantStoreApi {



  @Inject
  private StoreFacade storeFacade;

  @Inject
  private LanguageUtils languageUtils;

  @Inject
  LanguageService languageService;

  @Inject
  UserFacade userFacade;

  private static final Map<String, String> mappingFields =
      Stream
          .of(new AbstractMap.SimpleImmutableEntry<>("name", "storename"),
              new AbstractMap.SimpleImmutableEntry<>("readableAudit.user",
                  "auditSection.modifiedBy"))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  private static final Logger LOGGER = LoggerFactory.getLogger(MerchantStoreApi.class);

  @ResponseStatus(HttpStatus.OK)
  //@GetMapping(value = {"/store/{store}"}, produces ={ "application/json", "application/xml" })
  @GetMapping(value = {"/store/{store}"})
  @ApiOperation(httpMethod = "GET", value = "Get merchant store", notes = "",
     response = ReadableMerchantStore.class)
  public @ResponseBody ReadableMerchantStore store(@PathVariable String store,
      @RequestParam(value = "lang", required = false) String lang, HttpServletRequest request,
      HttpServletResponse response) {

    Language l = languageUtils.getServiceLanguage(lang);



    ReadableMerchantStore readableStore = storeFacade.getByCode(store, l);


    return readableStore;
  }

  @ResponseStatus(HttpStatus.OK)
  //@GetMapping(value = {"/private/store"}, produces ={ "application/json", "application/xml" })
  @GetMapping(value = {"/private/store"})
  @ApiOperation(httpMethod = "POST", value = "Creates a new store", notes = "",
      response = ReadableMerchantStore.class)
  public ResponseEntity<ReadableMerchantStore> create(
      @Valid @RequestBody PersistableMerchantStore store, HttpServletRequest request,
      HttpServletResponse response) throws Exception {



    // check if store code exists
    MerchantStore mStore = storeFacade.get(store.getCode());
    if (mStore != null) {
      response.sendError(503, "MerhantStore " + store.getCode() + " already exists");
    }

    try {

      storeFacade.create(store);

      ReadableMerchantStore readable =
          storeFacade.getByCode(store.getCode(), languageService.defaultLanguage());

      return new ResponseEntity<ReadableMerchantStore>(readable, HttpStatus.OK);

    } catch (Exception e) {
      LOGGER.error("Error while creating store ", e);
      try {
        response.sendError(503, "Error while creating store " + e.getMessage());
      } catch (Exception ignore) {
      }

      return null;
    }
  }

  @ResponseStatus(HttpStatus.OK)
  //@PutMapping(value = {"/private/store/{code}"}, produces ={ "application/json", "application/xml" })
  @PutMapping(value = {"/private/store/{code}"})
  @ApiOperation(httpMethod = "PUT", value = "Updates a store", notes = "",
      response = ReadableMerchantStore.class)
  public ResponseEntity<ReadableMerchantStore> update(
      @Valid @RequestBody PersistableMerchantStore store, HttpServletRequest request,
      HttpServletResponse response) throws Exception {



    try {

      // user doing action must be attached to the store being modified
      Principal principal = request.getUserPrincipal();
      String userName = principal.getName();

      if (!userFacade.authorizedStore(userName, store.getCode())) {
        response.sendError(401, "User " + userName + " not authorized");
        return null;
      }
      storeFacade.update(store);

      ReadableMerchantStore readable =
          storeFacade.getByCode(store.getCode(), languageService.defaultLanguage());

      return new ResponseEntity<ReadableMerchantStore>(readable, HttpStatus.OK);

    } catch (Exception e) {
      LOGGER.error("Error while updating store ", e);
      try {
        response.sendError(503, "Error while updating store " + e.getMessage());
      } catch (Exception ignore) {
      }

      return null;
    }
  }
  
  @ResponseStatus(HttpStatus.OK)
  //@GetMapping(value = {"/private/store/{code}/marketing"}, produces ={ "application/json", "application/xml" })
  @GetMapping(value = {"/private/store/{code}/marketing"})
  @ApiOperation(httpMethod = "GET", value = "Get store branding and marketing details", notes = "",
      response = ReadableMerchantStore.class)
  public ResponseEntity<ReadableMerchantStore> getStoreMarketing(
      @PathVariable String code, HttpServletRequest request,
      HttpServletResponse response) {

    try {

      // user doing action must be attached to the store being modified
      Principal principal = request.getUserPrincipal();
      String userName = principal.getName();

      if (!userFacade.authorizedStore(userName, code)) {
        throw new UnauthorizedException("User " + userName + " not authorized");
      }
      
      //get ReadableStore
      
      //get MerchantStoreConfiguration
      
      //BuildReadableMarketing

      return null;

    } catch (Exception e) {
      LOGGER.error("Error while updating store ", e);
      try {
        response.sendError(503, "Error while updating store " + e.getMessage());
      } catch (Exception ignore) {
      }

      return null;
    }
  }

  @ResponseStatus(HttpStatus.OK)
  //@GetMapping(value = {"/private/store/unique"}, produces ={ "application/json", "application/xml" })
  @GetMapping(value = {"/private/store/unique"})
  @ApiOperation(httpMethod = "GET", value = "Check if store code already exists", notes = "",
      response = EntityExists.class)
  public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code,
      HttpServletRequest request, HttpServletResponse response) throws Exception {



    try {


      MerchantStore store = storeFacade.get(code);

      EntityExists exists = new EntityExists();
      if (store != null) {
        exists.setExists(true);
      }
      return new ResponseEntity<EntityExists>(exists, HttpStatus.OK);

    } catch (Exception e) {
      LOGGER.error("Error while updating store ", e);
      try {
        response.sendError(503, "Error while getting store " + e.getMessage());
      } catch (Exception ignore) {
      }

      return null;
    }
  }

  @ResponseStatus(HttpStatus.OK)
  //@GetMapping(value = {"/private/stores"}, produces ={ "application/json", "application/xml" })
  @GetMapping(value = {"/private/stores"})
  @ApiOperation(httpMethod = "GET", value = "Check list of stores", notes = "",
      response = EntityExists.class)
  public ResponseEntity<ReadableMerchantStoreList> list(
      @RequestParam(value = "start", required = false) Integer start,
      @RequestParam(value = "length", required = false) Integer count,
      @RequestParam(value = "code", required = false) String code, HttpServletRequest request,
      HttpServletResponse response) throws Exception {



    try {

      // Principal principal = request.getUserPrincipal();
      // String userName = principal.getName();

/*      Enumeration names = request.getParameterNames();
      while (names.hasMoreElements()) {
        // System.out.println(names.nextElement().toString());
        String param = names.nextElement().toString();
        String val = request.getParameter(param);
        System.out.println("param ->" + param + " Val ->" + val);
      }*/

      MerchantStoreCriteria criteria = (MerchantStoreCriteria) ServiceRequestCriteriaBuilderUtils
          .buildRequest(mappingFields, request);

      if (start != null)
        criteria.setStartIndex(start);
      if (count != null)
        criteria.setMaxCount(count);
      if (!StringUtils.isBlank(criteria.getSearch())) {
        criteria.setCode(criteria.getSearch());
        criteria.setName(criteria.getSearch());
      }

      ReadableMerchantStoreList readableList =
          storeFacade.getByCriteria(criteria, languageService.defaultLanguage());
      readableList.setRecordsFiltered(readableList.getTotalCount());
      readableList.setRecordsTotal(readableList.getTotalCount());

      // datatable stuff
      String drawParam = request.getParameter("draw");
      if (!StringUtils.isEmpty(drawParam)) {
        readableList.setDraw(Integer.parseInt(request.getParameter("draw")));
      }


      return new ResponseEntity<ReadableMerchantStoreList>(readableList, HttpStatus.OK);

    } catch (Exception e) {
      LOGGER.error("Error while getting store list ", e);
      try {
        response.sendError(503, "Error while getting store list " + e.getMessage());
      } catch (Exception ignore) {
      }

      return null;
    }
  }

  @ResponseStatus(HttpStatus.OK)
  @RequestMapping(value = {"/private/store/{code}"}, method = RequestMethod.DELETE)
  @ApiOperation(httpMethod = "DELETE", value = "Deletes a store", notes = "",
      response = ResponseEntity.class)
  public ResponseEntity<Void> delete(@PathVariable String code, HttpServletRequest request,
      HttpServletResponse response) {

    // user doing action must be attached to the store being modified
    Principal principal = request.getUserPrincipal();
    String userName = principal.getName();

    try { // TODO remove trycatch

      if (!userFacade.authorizedStore(userName, code)) {
        // response.sendError(401, "User " + userName + " not authorized");
        // return null;
        throw new UnauthorizedException("Not authorized");
      }

    } catch (Exception e) {
      // todo to be removed
    }


    storeFacade.delete(code);
    return new ResponseEntity<Void>(HttpStatus.OK);

  }

}