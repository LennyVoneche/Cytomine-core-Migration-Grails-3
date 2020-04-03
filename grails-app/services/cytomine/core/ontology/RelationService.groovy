package cytomine.core.ontology

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

import cytomine.core.command.Transaction
import cytomine.core.project.Project
import cytomine.core.utils.ModelService
import cytomine.core.utils.Task
import grails.transaction.Transactional

//import cytomine.core.Exception.CytomineMethodNotYetImplementedException
/**
 * No security restriction for this domain (only read)
 */
@Transactional
class RelationService extends ModelService {


    def list() {
        Relation.list()
    }

    def read(def id) {
        Relation.read(id)
    }

    def readByName(String name) {
        Relation.findByName(name)
    }

    def getRelationParent() {
        readByName(RelationTerm.names.PARENT)
    }

    def deleteDependentRelationTerm(Project project, Transaction transaction, Task task = null) {
        //throw new CytomineMethodNotYetImplementedException("");
    }
}
