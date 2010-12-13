/*
 * jQuery wizard plug-in 2.0.1
 *
 *
 * Copyright (c) 2010 Jan Sundman (jan.sundman[at]aland.net)
 *
 * Licensed under the MIT licens:
 *   http://www.opensource.org/licenses/mit-license.php
 * 
 */
   
(function($){
	
  $.fn.formwizard = function(wizardSettings, validationSettings, formOptions){

	var form = $(this);
	var steps = form.find(".step");

	switch(arguments[0]){
		case "state":
			return getWizardState();
		case "reset":
			resetWizard();
			return $(this);
		case "show":
			(form[0].settings.historyEnabled)?$.historyLoad("_" + arguments[1].substr(arguments[1].indexOf("#") + 1)):show("_" + arguments[1].substr(arguments[1].indexOf("#") + 1));
			return $(this);
		case "destroy":
			destroy();
			return $(this);
		default:
			if(form[0].settings === undefined){
				initialize();
			}
	}

	function initialize(){
		var formOptionsSuccess = (formOptions)?formOptions.success:undefined;
		form[0].formSettings = $.extend(formOptions,{
		success	: function(data){ 
				if(formOptions && formOptions.resetForm || !formOptions){
					resetWizard();
				}
				if(formOptionsSuccess){
					formOptionsSuccess(data);
				}else{
					alert("success");
				}
			}
		});
	
		form[0].settings = $.extend({
			historyEnabled	: false,
			validationEnabled : false,
			formPluginEnabled : false,
			linkClass	: ".link",
			submitStepClass : "submit_step",
			back : ":reset",
			next : ":submit",
			textSubmit : 'Submit',
			textNext : 'Next',
			textBack : 'Back',
			afterNext: undefined,
			afterBack: undefined,
			serverSideValidationUrls : undefined,
			inAnimation : 'fadeIn',
			outAnimation : 'fadeOut',
			focusFirstInput : false,
			disableInputFields : true,
			showBackOnFirstStep : false
		}, wizardSettings);	
		
		form[0].activatedSteps = new Array();
		form[0].isLastStep = false;
		form[0].previousStep = undefined;
		form[0].currentStep = steps.eq(0).attr("id");	
		form[0].backButton	= form.find(form[0].settings.back);
		form[0].nextButton	= form.find(form[0].settings.next);	
		form[0].originalResetValue = form[0].backButton.val();
		form[0].originalSubmitValue = form[0].nextButton.val();

		if(form[0].settings.validationEnabled && jQuery().validate  == undefined){
			form[0].settings.validationEnabled = false;
			alert("the validation plugin needs to be included");
		}else if(form[0].settings.validationEnabled){
			form.validate(validationSettings);
		}
	
		if(form[0].settings.formPluginEnabled && jQuery().ajaxSubmit == undefined){
			form[0].settings.formPluginEnabled = false;
			alert("the form plugin needs to be included");
		}
		
		steps.hide();
		if(form[0].settings.disableInputFields == true){
			$(steps).find(":input").attr("disabled","disabled");
		}
		
		if(form[0].settings.historyEnabled && $.historyInit  == undefined){
			form[0].settings.historyEnabled = false;
			alert("the history plugin needs to be included");
		}else if(form[0].settings.historyEnabled){
			$.historyInit(show);
			$.historyLoad("_" + form[0].currentStep);
		}else{
			show(undefined);
		}
		form[0].initialized = true;
		form[0].backButton.val(form[0].settings.textBack);
		return $(this);
	}

	form[0].nextButton.click(function(){		
		if(form[0].settings.validationEnabled){
			if(!form.valid()){form.validate().focusInvalid();return false;}
		}
		if(form[0].isLastStep){ 
			for(var i = 0; i < form[0].activatedSteps.length; i++){
				steps.filter("#" + form[0].activatedSteps[i]).find(":input").removeAttr("disabled");
			}
			if(form[0].settings.formPluginEnabled){
				form[0].initialized = false;
				form.ajaxSubmit(form[0].formSettings);
				return false;
			}
			form.submit();
			return false;
		}

		if(form[0].settings.serverSideValidationUrls){
		  var options = form[0].settings.serverSideValidationUrls[form[0].currentStep];
			if(options != undefined){ 
			  var success = options.success;
				$.extend(options,{success: function(data, statusText){
						if((success != undefined && success(data, statusText)) || (success == undefined)){
							continueToNextStep();
						}
					}})
				form.ajaxSubmit(options);
				return false;
			}
		}
		continueToNextStep();
		
		return false;
	});

	form[0].backButton.click(function(){
		if(form[0].settings.historyEnabled && form[0].activatedSteps.length > 0){
			history.back();
		}else if(form[0].activatedSteps.length > 0){
			show("_" + form[0].activatedSteps[form[0].activatedSteps.length - 2]);
		}

		return false;
	});

	function continueToNextStep(){
		form[0].initialized = true;
		var step = navigate(form[0].currentStep);
		if(step == form[0].currentStep){
			animate(step,step);
			return;
		}
		if(form[0].settings.historyEnabled){
			$.historyLoad("_" + step);
		}else{
			show("_" + step);
		}
	}

	function disableNavigation(){
		form[0].nextButton.attr("disabled","disabled");
		form[0].backButton.attr("disabled","disabled");
	}
	
	function enableNavigation(){
		if(form[0].isLastStep){
			form[0].nextButton.val(form[0].settings.textSubmit);
		}else{
			form[0].nextButton.val(form[0].settings.textNext);
		}
		if(form[0].currentStep !== steps.eq(0).attr("id")){
			form[0].backButton.removeAttr("disabled").show();
		}else if(!form[0].settings.showBackOnFirstStep){
			form[0].backButton.hide();
		}
				
		form[0].nextButton.removeAttr("disabled");
	}

	function animate(oldStep, newStep){
		disableNavigation();
		var old = steps.filter("#" + oldStep);
		var current = steps.filter("#" + newStep);
		old.find(":input").attr("disabled","disabled");
		current.find(":input").removeAttr("disabled");

		if(form[0].settings.historyEnabled){
			old[form[0].settings.outAnimation](0);
			current[form[0].settings.inAnimation](400,function(){
				if(form[0].settings.focusFirstInput)
						current.find(":input:first").focus();
				enableNavigation();						
			});
			return;
		}
		
		old[form[0].settings.outAnimation](400, function(){
			current[form[0].settings.inAnimation](400,function(){
				if(form[0].settings.focusFirstInput)
					current.find(":input:first").focus();
				enableNavigation();
			});		
		});
	}

	function checkIflastStep(step){
		form[0].isLastStep = false;
		if($("#" + step).hasClass(form[0].settings.submitStepClass) || steps.filter(":last").attr("id") == step){
			form[0].isLastStep = true;
		}
	}

	function getLink(step){
		var link = undefined;
		var links = steps.filter("#" + step).find(form[0].settings.linkClass);

		if(links != undefined && links.length == 1){
			link = $(links).val();
		}else if(links != undefined && links.length > 1){ 
			link = links.filter(form[0].settings.linkClass + ":checked").val();
		}else{
			link = undefined;
		}
		return link;
	}

	function navigate(step){
		var link = getLink(step);
		if(link != undefined){
			if((link != "" && link != null && link != undefined) && steps.filter("#" + link).attr("id") != undefined){
				return link;
			}
			return form[0].currentStep;				
		}else if(link == undefined && !form[0].isLastStep){	
			var currentStepIndex = steps.index($("#" + form[0].currentStep));
			return steps.filter(":eq(" + (1 * currentStepIndex + 1)  + ")").attr("id");
		}
	}
	
	function show(step){
		var backwards = false;
		if(step == undefined || step == ""){ 
				form[0].activatedSteps.pop();
				step = $(steps).eq(0).attr("id");
				form[0].activatedSteps.push(step);
		}else{	
			step = step.substr(1);
			if(form[0].activatedSteps[form[0].activatedSteps.length - 2] == step){
				backwards = true;
				form[0].activatedSteps.pop();
			}else {
				form[0].activatedSteps.push(step);
			}
		}
		var oldStep = form[0].currentStep;
		checkIflastStep(step);
		form[0].previousStep = oldStep;
		form[0].currentStep = step;
		animate(oldStep, step);	
		if(backwards){
			if(form[0].settings.afterBack)	
				form[0].settings.afterBack({"currentStep" : form[0].currentStep, 
													"previousStep" : form[0].previousStep,
													"isLastStep" : form[0].isLastStep, 
													"activatedSteps" : form[0].activatedSteps});
		}else if(form[0].initialized){
			if(form[0].settings.afterNext)
				form[0].settings.afterNext({"currentStep" : form[0].currentStep, 
														"previousStep" : form[0].previousStep,
														"isLastStep" : form[0].isLastStep, 
														"activatedSteps" : form[0].activatedSteps});
		}
		
	}
		
	function resetWizard(){
		form[0].reset()
		$("label,:input,textarea",this).removeClass("error");		
		for(var i = 0; i < form[0].activatedSteps.length; i++){
			steps.filter("#" + form[0].activatedSteps[i]).hide().find(":input").attr("disabled","disabled");
		}
		form[0].activatedSteps = new Array();
		form[0].previousStep = undefined;	
		form[0].isLastStep = false;	
		var step = steps.eq(0).attr("id");
		if(form[0].settings.historyEnabled){
			$.historyLoad("_" + step);
		}else{
			show("_" + step);
		}		
	}
	
	function getWizardState(){
		return { "settings" : form[0].settings,
			"activatedSteps" : form[0].activatedSteps,
			"isLastStep" : form[0].isLastStep,
			"previousStep" : form[0].previousStep,
			"currentStep" : form[0].currentStep,
			"backButton" : form[0].backButton,
			"nextButton" :form[0].nextButton
		}
	}
	
	function destroy(){
		form[0].reset();
		$(steps).show();
		if(form[0].settings.disableInputFields == true){
			$(steps).find(":input").removeAttr("disabled");
		}

		form[0].backButton.removeAttr("disabled").val(form[0].originalResetValue).show();
		form[0].nextButton.val(form[0].originalSubmitValue);
		form[0].nextButton.unbind("click");
		form[0].backButton.unbind("click")
		form[0].activatedSteps = undefined;
		form[0].previousStep = undefined;
		form[0].currentStep = undefined;
		form[0].isLastStep = undefined;
		form[0].settings = undefined;
		form[0].nextButton = undefined;
		form[0].backButton = undefined;
		form[0].formwizard = undefined;
		form[0] = undefined;
		steps = undefined;
		form = undefined;
	}
};
})(jQuery);
