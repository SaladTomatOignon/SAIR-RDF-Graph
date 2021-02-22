# SAIR-RDF-Graph
(Symbolic AI and Reasoning project) - Creation of a RDF Graph from Covid-19 vaccines data, analyzes with SPARQL queries. Creation and uses of a set of ontologies based from this data, finally displaying some visualization

##  Part. 1 - Requests on extended data
 

- PREFIX lubm1: <<http://swat.cse.lehigh.edu/onto/univ-bench.owl#>>
- PREFIX ext: <<http://extension.group4.fr/onto#>>

#####  1. Count the number of vaccined and not-vaccined persons
  
###### 1.1 : Number of vaccinated

    SELECT DISTINCT (COUNT(?s) as ?total) WHERE {
		?s ext:vaccinationDate ?o
	}

###### 1.2 : Number of not-vaccinated

    SELECT DISTINCT (COUNT(?s) as ?total) WHERE {
		?s !ext:vaccinationDate ?o
	}

 ##### 2.  For the vaccined persons, retrieve the number of individuals in in vaccine groups, that is a group is a given vaccine drug.

    SELECT (?o as ?vaccine) (COUNT(?s) as ?total) WHERE {
		?s ext:vaccine ?o
	}
	GROUP BY ?o

##### 3. Write a query that retrieves AssociateProfessors that work for an organization that is a Department and who teach a course. Using the“Explain” advanced features in the Query Query tab, interpret the provided query plan.

    SELECT DISTINCT (?s as ?AssociateProfessor) WHERE {
		?s rdf:type lubm1:AssociateProfessor .
		?dep rdf:type lubm1:Department .
		?s lubm1:worksFor ?dep .
		?s lubm1:teacherOf ?o
	}

Le plan d'exécution nous indique simplement qu'il a effectué 4 jointures.
##  Part. 2 - Requests on inferenced data
- PREFIX lubm1: <<http://swat.cse.lehigh.edu/onto/univ-bench.owl#>>
- PREFIX ext: <<http://extension.group4.fr/onto#>>

##### 1. Retrieve the last and first names of all professors. Note how many professors you are retrieving

    SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?s rdf:type lubm1:Professor } UNION
		{ ?s rdf:type lubm1:Lecturer }

		?s ext:fName ?fName .
		?s ext:lName ?lName
	}
	GROUP BY ?s

=> 540 lines retrieved

##### 2. Retrieve the last and first names of all professors that are advisors of some students.

    SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?prof rdf:type lubm1:Professor } UNION
		{ ?prof rdf:type lubm1:Lecturer }

		{ ?student rdf:type lubm1:Student } UNION
		{ ?student rdf:type lubm1:GraduateStudent }

		?student lubm1:advisor ?prof .
		?prof ext:fName ?fName .
		?prof ext:lName ?lName
	}
	GROUP BY ?prof

=> 445 lines retrieved

##### 3. Retrieve the last and first names of professors that are member of an organization


	SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?s rdf:type lubm1:Professor } UNION
		{ ?s rdf:type lubm1:Lecturer }

		?s lubm1:worksFor ?organization .
		?s ext:fName ?fName .
		?s ext:lName ?lName
	}
	GROUP BY ?s

=> 540 lines retrieved

##### 4. Retrieve the last and first names of professors that are member of an organization and advise a student.

    SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?prof rdf:type lubm1:Professor } UNION
		{ ?prof rdf:type lubm1:Lecturer }

		{ ?student rdf:type lubm1:Student } UNION
		{ ?student rdf:type lubm1:GraduateStudent }

		?student lubm1:advisor ?prof .
		?prof lubm1:worksFor ?organization .
		?prof ext:fName ?fName .
		?prof ext:lName ?lName
	}
	GROUP BY ?prof

=> 445 lines retrieved

##### 5.  In a non-inference Blazegraph configuration, rewrite the queries 3.2, 3.3 and 3.4 in such a way that it produces the same answer set as its respective original query

###### A. Professors that are advisors of some students

    SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?prof rdf:type lubm1:AssociateProfessor } UNION
		{ ?prof rdf:type lubm1:FullProfessor } UNION
		{ ?prof rdf:type lubm1:Lecturer } UNION
		{ ?prof rdf:type lubm1:AssistantProfessor }

		{ ?student rdf:type lubm1:GraduateStudent } UNION
		{ ?student rdf:type lubm1:UndergraduateStudent }

		?student lubm1:advisor ?prof .
		?prof ext:fName ?fName  .
		?prof ext:lName ?lName
	}
	GROUP BY ?prof

###### B. Professors that are member of an organization

    SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?s rdf:type lubm1:AssociateProfessor } UNION
		{ ?s rdf:type lubm1:FullProfessor } UNION
		{ ?s rdf:type lubm1:Lecturer } UNION
		{ ?s rdf:type lubm1:AssistantProfessor }

		?s lubm1:worksFor ?organization .
		?s ext:fName ?fName .
		?s ext:lName ?lName
	}
	GROUP BY ?s

###### C. Professors that are member of an organization and advise a student

    SELECT (SAMPLE(?fName) as ?firstName) (SAMPLE(?lName) as ?lastName) WHERE {
		{ ?prof rdf:type lubm1:AssociateProfessor } UNION
		{ ?prof rdf:type lubm1:FullProfessor } UNION
		{ ?prof rdf:type lubm1:Lecturer } UNION
		{ ?prof rdf:type lubm1:AssistantProfessor }

		{ ?student rdf:type lubm1:GraduateStudent } UNION
		{ ?student rdf:type lubm1:UndergraduateStudent }

		?student lubm1:advisor ?prof .
		?prof lubm1:worksFor ?organization .
		?prof ext:fName ?fName .
		?prof ext:lName ?lName
	}
	GROUP BY ?prof

