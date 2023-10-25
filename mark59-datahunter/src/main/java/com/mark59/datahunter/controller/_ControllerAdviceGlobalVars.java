package com.mark59.datahunter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


/**
 * The @ControllerAdvice annotation means this class is actioned for every controller, 
 * so the @ModelAttribute will be populated for all jsps. Handy here as we need a value
 * to be universally available to the navigation jsp, to decide whether to present
 * the h2 console link or not.
 * <p>
 * References:<br>
 * https://stackoverflow.com/questions/33876699/how-to-make-a-model-attribute-global<br>
 */
@ControllerAdvice
public class _ControllerAdviceGlobalVars {

	@Autowired
	String currentDatabaseProfile;

	@ModelAttribute("currentDatabaseProfile")
	public String populateUser() {
		return currentDatabaseProfile;
	}

}
