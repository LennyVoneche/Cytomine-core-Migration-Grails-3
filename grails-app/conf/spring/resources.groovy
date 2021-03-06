package spring
/*
* Copyright (c) 2009-2019. Authors: see NOTICE file.
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

import cytomine.core.LogoutEventListener
import cytomine.core.ldap.CustomUserContextMapper
import cytomine.core.security.CASLdapUserDetailsService
import cytomine.core.security.SimpleUserDetailsService
import cytomine.core.spring.CustomAjaxAwareAuthenticationEntryPoint
import cytomine.core.spring.CustomDefaultRedirectStrategy
import cytomine.core.spring.CustomSavedRequestAwareAuthenticationSuccessHandler
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.util.Holders
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler

import grails.plugin.springsecurity.SpringSecurityUtils
//import cytomine.web.APIAuthentificationFilters
import cytomine.web.LoginInterceptor
// Place your Spring DSL code here
beans = {
    println getClass().toString() + '001' + Holders.config.info.app.versionDate
    logoutEventListener(LogoutEventListener)
//    'apiAuthentificationFilter'(LoginInterceptor) {
//         properties
//    }
//    'multipartResolver'(CytomineMultipartHttpServletRequest) {
//        // Max in memory 100kbytes
//        maxInMemorySize=10240
//
//        //100Gb Max upload size
//        maxUploadSize=102400000000
//    }
    println getClass().toString() + '002'

    springConfig.addAlias "springSecurityService", "springSecurityCoreSpringSecurityService"
    println getClass().toString() + '003'

    //CAS + LDAP STUFF
    def config = SpringSecurityUtils.securityConfig
    SpringSecurityUtils.loadSecondaryConfig 'DefaultLdapSecurityConfig'
    config = SpringSecurityUtils.securityConfig

    println getClass().toString() + '004'

    redirectStrategy(CustomDefaultRedirectStrategy) {
        contextRelative = true
    }
    successRedirectHandler(CustomSavedRequestAwareAuthenticationSuccessHandler) {
        alwaysUseDefaultTargetUrl = false
        //defaultTargetUrl = '/'
    }

    authenticationEntryPoint(CustomAjaxAwareAuthenticationEntryPoint, config.auth.loginFormUrl) {
        grailsApplication = ref('grailsApplication')
        ajaxLoginFormUrl = '/login/authAjax'
        forceHttps = false
        useForward = false
        portMapper = ref('portMapper')
        portResolver = ref('portResolver')
    }
    println getClass().toString() + '005'+ Holders.getGrailsApplication().config.grails.serverURL + " :-)"

    authenticationSuccessHandler(AjaxAwareAuthenticationSuccessHandler) {
        requestCache = ref('requestCache')
        defaultTargetUrl = Holders.getGrailsApplication().config.grails.UIURL?: Holders.getGrailsApplication().config.grails.serverURL ?: '/'
        alwaysUseDefaultTargetUrl = false
        targetUrlParameter = 'spring-security-redirect'
        ajaxSuccessUrl = SpringSecurityUtils.securityConfig.successHandler.ajaxSuccessUrl
        useReferer = false
        redirectStrategy = ref('redirectStrategy')
    }

    logoutSuccessHandler(SimpleUrlLogoutSuccessHandler) {
        defaultTargetUrl = Holders.getGrailsApplication().config.grails.UIURL?: Holders.getGrailsApplication().config.grails.serverURL ?: '/'
    }

    println getClass().toString() + '006'

    if(config.ldap.active){
        initialDirContextFactory(org.springframework.security.ldap.DefaultSpringSecurityContextSource,
                config.ldap.context.server){
            userDn = config.ldap.context.managerDn
            password = config.ldap.context.managerPassword
            anonymousReadOnly = config.ldap.context.anonymousReadOnly
        }

        ldapUserSearch(org.springframework.security.ldap.search.FilterBasedLdapUserSearch,
                config.ldap.search.base,
                config.ldap.search.filter,
                initialDirContextFactory){
        }

        ldapAuthoritiesPopulator(org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator,
                initialDirContextFactory,
                config.ldap.authorities.groupSearchBase){
            groupRoleAttribute = config.ldap.authorities.groupRoleAttribute
            groupSearchFilter = config.ldap.authorities.groupSearchFilter
            searchSubtree = config.ldap.authorities.searchSubtree
            convertToUpperCase = config.ldap.mapper.convertToUpperCase
            ignorePartialResultException = config.ldap.authorities.ignorePartialResultException
        }

        ldapUserDetailsMapper(CustomUserContextMapper)

        ldapUserDetailsService(org.springframework.security.ldap.userdetails.LdapUserDetailsService,
                ldapUserSearch,
                ldapAuthoritiesPopulator){
            userDetailsMapper = ref('ldapUserDetailsMapper')
        }

        userDetailsService(CASLdapUserDetailsService) {
            ldapUserDetailsService=ref('ldapUserDetailsService')
            grailsApplication = ref('grailsApplication')
        }
    } else {

        println getClass().toString() + '007  userDetailsService(SimpleUserDetailsService)'
        userDetailsService(SimpleUserDetailsService)
    }
    println getClass().toString() + '007'

    ehcacheAclCache(EhCacheFactoryBean) {
        cacheManager = ref('aclCacheManager')
        cacheName = 'aclCache'
        overflowToDisk = false
    }
    println getClass().toString() + '008'

    currentRoleServiceProxy(org.springframework.aop.scope.ScopedProxyFactoryBean) {
        targetBeanName = 'currentRoleService'
        proxyTargetClass = true
    }
    println getClass().toString() + 'Fin resources.groovy'

}
