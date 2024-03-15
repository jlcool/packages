// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.inapppurchase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.AccountIdentifiers;
import com.android.billingclient.api.AlternativeBillingOnlyReportingDetails;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingConfig;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.UserChoiceDetails;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Handles serialization and deserialization of {@link com.android.billingclient.api.BillingClient}
 * related objects.
 */
/*package*/ class Translator {
  static HashMap<String, Object> fromProductDetail(ProductDetails detail) {
    HashMap<String, Object> info = new HashMap<>();
    info.put("title", detail.getTitle());
    info.put("description", detail.getDescription());
    info.put("productId", detail.getProductId());
    info.put("productType", detail.getProductType());
    info.put("name", detail.getName());

    @Nullable
    ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails =
        detail.getOneTimePurchaseOfferDetails();
    if (oneTimePurchaseOfferDetails != null) {
      info.put(
          "oneTimePurchaseOfferDetails",
          fromOneTimePurchaseOfferDetails(oneTimePurchaseOfferDetails));
    }

    @Nullable
    List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList =
        detail.getSubscriptionOfferDetails();
    if (subscriptionOfferDetailsList != null) {
      info.put(
          "subscriptionOfferDetails",
          fromSubscriptionOfferDetailsList(subscriptionOfferDetailsList));
    }

    return info;
  }

  static List<QueryProductDetailsParams.Product> toProductList(
      List<Messages.PlatformQueryProduct> platformProducts) {
    List<QueryProductDetailsParams.Product> products = new ArrayList<>();
    for (Messages.PlatformQueryProduct platformProduct : platformProducts) {
      products.add(toProduct(platformProduct));
    }
    return products;
  }

  static QueryProductDetailsParams.Product toProduct(
      Messages.PlatformQueryProduct platformProduct) {

    return QueryProductDetailsParams.Product.newBuilder()
        .setProductId(platformProduct.getProductId())
        .setProductType(toProductTypeString(platformProduct.getProductType()))
        .build();
  }

  static String toProductTypeString(Messages.PlatformProductType type) {
    switch (type) {
      case INAPP:
        return BillingClient.ProductType.INAPP;
      case SUBS:
        return BillingClient.ProductType.SUBS;
    }
    throw new Messages.FlutterError("UNKNOWN_TYPE", "Unknown product type: " + type, null);
  }

  static Messages.PlatformProductType toPlatformProductType(String typeString) {
    switch (typeString) {
      case BillingClient.ProductType.INAPP:
        // Fallback handling to avoid throwing an exception if a new type is added in the future.
      default:
        return Messages.PlatformProductType.INAPP;
      case BillingClient.ProductType.SUBS:
        return Messages.PlatformProductType.SUBS;
    }
  }

  static List<Object> fromProductDetailsList(@Nullable List<ProductDetails> productDetailsList) {
    if (productDetailsList == null) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    ArrayList<Object> output = new ArrayList<>();
    for (ProductDetails detail : productDetailsList) {
      output.add(fromProductDetail(detail));
    }
    return output;
  }

  static HashMap<String, Object> fromOneTimePurchaseOfferDetails(
      @Nullable ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails) {
    HashMap<String, Object> serialized = new HashMap<>();
    if (oneTimePurchaseOfferDetails == null) {
      return serialized;
    }

    serialized.put("priceAmountMicros", oneTimePurchaseOfferDetails.getPriceAmountMicros());
    serialized.put("priceCurrencyCode", oneTimePurchaseOfferDetails.getPriceCurrencyCode());
    serialized.put("formattedPrice", oneTimePurchaseOfferDetails.getFormattedPrice());

    return serialized;
  }

  static List<HashMap<String, Object>> fromSubscriptionOfferDetailsList(
      @Nullable List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList) {
    if (subscriptionOfferDetailsList == null) {
      return Collections.emptyList();
    }

    ArrayList<HashMap<String, Object>> serialized = new ArrayList<>();

    for (ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails :
        subscriptionOfferDetailsList) {
      serialized.add(fromSubscriptionOfferDetails(subscriptionOfferDetails));
    }

    return serialized;
  }

  static HashMap<String, Object> fromSubscriptionOfferDetails(
      @Nullable ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails) {
    HashMap<String, Object> serialized = new HashMap<>();
    if (subscriptionOfferDetails == null) {
      return serialized;
    }

    serialized.put("offerId", subscriptionOfferDetails.getOfferId());
    serialized.put("basePlanId", subscriptionOfferDetails.getBasePlanId());
    serialized.put("offerTags", subscriptionOfferDetails.getOfferTags());
    serialized.put("offerIdToken", subscriptionOfferDetails.getOfferToken());

    ProductDetails.PricingPhases pricingPhases = subscriptionOfferDetails.getPricingPhases();
    serialized.put("pricingPhases", fromPricingPhases(pricingPhases));

    return serialized;
  }

  static List<HashMap<String, Object>> fromPricingPhases(
      @NonNull ProductDetails.PricingPhases pricingPhases) {
    ArrayList<HashMap<String, Object>> serialized = new ArrayList<>();

    for (ProductDetails.PricingPhase pricingPhase : pricingPhases.getPricingPhaseList()) {
      serialized.add(fromPricingPhase(pricingPhase));
    }
    return serialized;
  }

  static HashMap<String, Object> fromPricingPhase(
      @Nullable ProductDetails.PricingPhase pricingPhase) {
    HashMap<String, Object> serialized = new HashMap<>();

    if (pricingPhase == null) {
      return serialized;
    }

    serialized.put("formattedPrice", pricingPhase.getFormattedPrice());
    serialized.put("priceCurrencyCode", pricingPhase.getPriceCurrencyCode());
    serialized.put("priceAmountMicros", pricingPhase.getPriceAmountMicros());
    serialized.put("billingCycleCount", pricingPhase.getBillingCycleCount());
    serialized.put("billingPeriod", pricingPhase.getBillingPeriod());
    serialized.put("recurrenceMode", pricingPhase.getRecurrenceMode());

    return serialized;
  }

  static Messages.PlatformPurchaseState toPlatformPurchaseState(int state) {
    switch (state) {
      case Purchase.PurchaseState.PURCHASED:
        return Messages.PlatformPurchaseState.PURCHASED;
      case Purchase.PurchaseState.PENDING:
        return Messages.PlatformPurchaseState.PENDING;
      case Purchase.PurchaseState.UNSPECIFIED_STATE:
        return Messages.PlatformPurchaseState.UNSPECIFIED;
    }
    return Messages.PlatformPurchaseState.UNSPECIFIED;
  }

  static Messages.PlatformPurchase fromPurchase(Purchase purchase) {
    Messages.PlatformPurchase.Builder builder =
        new Messages.PlatformPurchase.Builder()
            .setOrderId(purchase.getOrderId())
            .setPackageName(purchase.getPackageName())
            .setPurchaseTime(purchase.getPurchaseTime())
            .setPurchaseToken(purchase.getPurchaseToken())
            .setSignature(purchase.getSignature())
            .setProducts(purchase.getProducts())
            .setIsAutoRenewing(purchase.isAutoRenewing())
            .setOriginalJson(purchase.getOriginalJson())
            .setDeveloperPayload(purchase.getDeveloperPayload())
            .setIsAcknowledged(purchase.isAcknowledged())
            .setPurchaseState(toPlatformPurchaseState(purchase.getPurchaseState()))
            .setQuantity((long) purchase.getQuantity());
    AccountIdentifiers accountIdentifiers = purchase.getAccountIdentifiers();
    if (accountIdentifiers != null) {
      builder.setAccountIdentifiers(
          new Messages.PlatformAccountIdentifiers.Builder()
              .setObfuscatedAccountId(accountIdentifiers.getObfuscatedAccountId())
              .setObfuscatedProfileId(accountIdentifiers.getObfuscatedProfileId())
              .build());
    }
    return builder.build();
  }

  static Messages.PlatformPurchaseHistoryRecord fromPurchaseHistoryRecord(
      PurchaseHistoryRecord purchaseHistoryRecord) {
    return new Messages.PlatformPurchaseHistoryRecord.Builder()
        .setPurchaseTime(purchaseHistoryRecord.getPurchaseTime())
        .setPurchaseToken(purchaseHistoryRecord.getPurchaseToken())
        .setSignature(purchaseHistoryRecord.getSignature())
        .setProducts(purchaseHistoryRecord.getProducts())
        .setDeveloperPayload(purchaseHistoryRecord.getDeveloperPayload())
        .setOriginalJson(purchaseHistoryRecord.getOriginalJson())
        .setQuantity((long) purchaseHistoryRecord.getQuantity())
        .build();
  }

  static List<Messages.PlatformPurchase> fromPurchasesList(@Nullable List<Purchase> purchases) {
    if (purchases == null) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    List<Messages.PlatformPurchase> serialized = new ArrayList<>();
    for (Purchase purchase : purchases) {
      serialized.add(fromPurchase(purchase));
    }
    return serialized;
  }

  static List<Messages.PlatformPurchaseHistoryRecord> fromPurchaseHistoryRecordList(
      @Nullable List<PurchaseHistoryRecord> purchaseHistoryRecords) {
    if (purchaseHistoryRecords == null) {
      return Collections.emptyList();
    }

    // This and the method are generically typed due to Pigeon limitations; see
    // https://github.com/flutter/flutter/issues/116117.
    List<Messages.PlatformPurchaseHistoryRecord> serialized = new ArrayList<>();
    for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecords) {
      serialized.add(fromPurchaseHistoryRecord(purchaseHistoryRecord));
    }
    return serialized;
  }

  static Messages.PlatformBillingResult fromBillingResult(BillingResult billingResult) {
    return new Messages.PlatformBillingResult.Builder()
        .setResponseCode((long) billingResult.getResponseCode())
        .setDebugMessage(billingResult.getDebugMessage())
        .build();
  }

  static Messages.PlatformUserChoiceDetails fromUserChoiceDetails(
      UserChoiceDetails userChoiceDetails) {
    return new Messages.PlatformUserChoiceDetails.Builder()
        .setExternalTransactionToken(userChoiceDetails.getExternalTransactionToken())
        .setOriginalExternalTransactionId(userChoiceDetails.getOriginalExternalTransactionId())
        .setProducts(fromUserChoiceProductsList(userChoiceDetails.getProducts()))
        .build();
  }

  static List<Messages.PlatformUserChoiceProduct> fromUserChoiceProductsList(
      List<UserChoiceDetails.Product> productsList) {
    if (productsList.isEmpty()) {
      return Collections.emptyList();
    }

    ArrayList<Messages.PlatformUserChoiceProduct> output = new ArrayList<>();
    for (UserChoiceDetails.Product product : productsList) {
      output.add(fromUserChoiceProduct(product));
    }
    return output;
  }

  static Messages.PlatformUserChoiceProduct fromUserChoiceProduct(
      UserChoiceDetails.Product product) {
    return new Messages.PlatformUserChoiceProduct.Builder()
        .setId(product.getId())
        .setOfferToken(product.getOfferToken())
        .setType(toPlatformProductType(product.getType()))
        .build();
  }

  /** Converter from {@link BillingResult} and {@link BillingConfig} to map. */
  static Messages.PlatformBillingConfigResponse fromBillingConfig(
      BillingResult result, BillingConfig billingConfig) {
    return new Messages.PlatformBillingConfigResponse.Builder()
        .setBillingResult(fromBillingResult(result))
        .setCountryCode(billingConfig.getCountryCode())
        .build();
  }

  /**
   * Converter from {@link BillingResult} and {@link AlternativeBillingOnlyReportingDetails} to map.
   */
  static Messages.PlatformAlternativeBillingOnlyReportingDetailsResponse
      fromAlternativeBillingOnlyReportingDetails(
          BillingResult result, AlternativeBillingOnlyReportingDetails details) {
    return new Messages.PlatformAlternativeBillingOnlyReportingDetailsResponse.Builder()
        .setBillingResult(fromBillingResult(result))
        .setExternalTransactionToken(details.getExternalTransactionToken())
        .build();
  }

  /**
   * Gets the symbol of for the given currency code for the default {@link Locale.Category#DISPLAY
   * DISPLAY} locale. For example, for the US Dollar, the symbol is "$" if the default locale is the
   * US, while for other locales it may be "US$". If no symbol can be determined, the ISO 4217
   * currency code is returned.
   *
   * @param currencyCode the ISO 4217 code of the currency
   * @return the symbol of this currency code for the default {@link Locale.Category#DISPLAY
   *     DISPLAY} locale
   * @exception NullPointerException if <code>currencyCode</code> is null
   * @exception IllegalArgumentException if <code>currencyCode</code> is not a supported ISO 4217
   *     code.
   */
  static String currencySymbolFromCode(String currencyCode) {
    return Currency.getInstance(currencyCode).getSymbol();
  }
}
