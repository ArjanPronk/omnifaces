/*
 * Copyright 2013 OmniFaces.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnifaces.cdi.validator;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;

import org.omnifaces.application.ValidatorProvider;

/**
 * Collect all classes having {@link FacesValidator} annotation by their ID.
 *
 * @author Radu Creanga <rdcrng@gmail.com>
 * @author Bauke Scholtz
 * @see ValidatorProvider
 * @since 1.6
 */
public class ValidatorExtension implements Extension {

	// Constants ------------------------------------------------------------------------------------------------------

	private static final String ERROR_DUPLICATE_ID =
		"Registering validator '%s' failed, duplicates validator ID '%s' of other validator '%s'.";

	// Properties -----------------------------------------------------------------------------------------------------

	private Map<String, Bean<Validator>> validatorsById = new HashMap<String, Bean<Validator>>();

	// Actions --------------------------------------------------------------------------------------------------------

	/**
	 * Check if the processed {@link Validator} instance has the {@link FacesValidator} annotation and if so, then
	 * collect it by its ID.
	 * @param validator The processed {@link Validator} instance.
	 */
	protected void processValidators(@Observes ProcessManagedBean<Validator> validator) {
		FacesValidator annotation = validator.getAnnotatedBeanClass().getAnnotation(FacesValidator.class);

		if (annotation == null) {
			return;
		}

		Bean<Validator> currentBean = validator.getBean();
		String validatorId = annotation.value();
		Bean<Validator> previousBean = validatorsById.put(validatorId, currentBean);

		if (previousBean != null) {
			validator.addDefinitionError(new IllegalArgumentException(String.format(
				ERROR_DUPLICATE_ID, currentBean.getBeanClass(), validatorId, previousBean.getBeanClass())));
		}
	}

	// Getters --------------------------------------------------------------------------------------------------------

	/**
	 * Returns a mapping of all registered {@link FacesValidator}s by their validator ID.
	 * @return A mapping of all registered {@link FacesValidator}s by their validator ID.
	 */
	Map<String, Bean<Validator>> getValidatorsById() {
		return validatorsById;
	}

}