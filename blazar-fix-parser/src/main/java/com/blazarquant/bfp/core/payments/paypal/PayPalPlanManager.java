package com.blazarquant.bfp.core.payments.paypal;

import com.blazarquant.bfp.core.payments.exception.PaymentException;
import com.blazarquant.bfp.core.payments.SubscriptionPlan;
import com.blazarquant.bfp.core.payments.paypal.util.PayPalPlanInitializer;
import com.paypal.api.payments.Plan;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Wojciech Zankowski
 */
public class PayPalPlanManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayPalPlanManager.class);

    private final Map<SubscriptionPlan, Plan> payPalPlanMap = new ConcurrentHashMap<>();
    private final PayPalPlanInitializer planInitializer;

    public PayPalPlanManager(String accessToken) throws IOException, PayPalRESTException {
        this.planInitializer = new PayPalPlanInitializer(accessToken);
        new Thread(() -> {
            try {
                initialize();
            } catch (IOException | PayPalRESTException e) {
                LOGGER.error("Failed to initialize plan. ", e);
            }
        }).start();
    }

    private void initialize() throws IOException, PayPalRESTException {
        payPalPlanMap.put(SubscriptionPlan.PRO, planInitializer.initializeProPlan());
        payPalPlanMap.put(SubscriptionPlan.ENTERPRISE, planInitializer.initializeEnterprisePlan());
    }

    protected Plan getPlan(SubscriptionPlan payPalPlan) throws PaymentException {
        Plan plan = payPalPlanMap.get(payPalPlan);
        if (plan == null) {
            throw new PaymentException("Failed to retrive " + payPalPlan.name() + " plan.");
        }
        return plan;
    }

}