Please cite Xenon (1.2.2) as follows:

- [BibTeX](https://github.com/NLeSC/Xenon/blob/citation/CITATION.md#bibtex)
- [CSL](https://github.com/NLeSC/Xenon/blob/citation/CITATION.md#csl)
- [DataCite](https://github.com/NLeSC/Xenon/blob/citation/CITATION.md#datacite)
- [Dublin Core](https://github.com/NLeSC/Xenon/blob/citation/CITATION.md#dublin-core)
- [JSON](https://github.com/NLeSC/Xenon/blob/citation/CITATION.md#json)
- [MARKCXML](https://github.com/NLeSC/Xenon/blob/citation/CITATION.md#markcxml)


# BibTeX

```text
@misc{jason_maassen_2017_572636,
  author       = {Jason Maassen and
                  Stefan Verhoeven and
                  Joris Borgdorff and
                  Niels Drost and
                  Christiaan Meijer and
                  Jurriaan H. Spaaks and
                  Rob V. van Nieuwpoort and
                  Piter T. de Boer and
                  Ben van Werkhoven},
  title        = {NLeSC/Xenon: Xenon 1.2.2},
  month        = may,
  year         = 2017,
  doi          = {10.5281/zenodo.572636},
  url          = {https://doi.org/10.5281/zenodo.572636}
}
```

# CSL


```text
{
  "DOI": "10.5281/zenodo.572636", 
  "abstract": "<p>This is release 1.2.2 of Xenon.</p>\n<p>Bugfixes:</p>\n<ul>\n<li>fixed bug in the copy engine that would ignore a copy if source and destination had exactly the same path (even on different machines).</li>\n<li>added timeout overflow detection in Jobs.waitUntilDone and Jobs.waitUntilRunning.</li>\n</ul>\n<p>Other changes:</p>\n<ul>\n<li>we have a new logo!</li>\n<li>added SonarQube code for quality analysis and coverage</li>\n</ul>\n<p>What's missing:</p>\n<p>The GridFTP adaptor is not considered stable yet. It is not part of this release.</p>\n<p>There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.</p>\n<p>It should be made easier to inspect at runtime which adaptors are available and what properties they support.</p>\n<p>We can always use more adaptors, e.g, for S3, SWIFT, HDFS, YARN, Azure-Batch, Amazon-Batch etc. These are planned for 1.3 or later.</p>\n<p>We can always use more interfaces, e.g. for clouds. This is planned for 2.0.</p>", 
  "author": [
    {
      "family": "Jason Maassen"
    }, 
    {
      "family": "Stefan Verhoeven"
    }, 
    {
      "family": "Joris Borgdorff"
    }, 
    {
      "family": "Niels Drost"
    }, 
    {
      "family": "Christiaan Meijer"
    }, 
    {
      "family": "Jurriaan H. Spaaks"
    }, 
    {
      "family": "Rob V. van Nieuwpoort"
    }, 
    {
      "family": "Piter T. de Boer"
    }, 
    {
      "family": "Ben van Werkhoven"
    }
  ], 
  "id": "572636", 
  "issued": {
    "date-parts": [
      [
        2017, 
        5, 
        8
      ]
    ]
  }, 
  "publisher": "Zenodo", 
  "title": "NLeSC/Xenon: Xenon 1.2.2", 
  "type": "article"
}
```


# DataCite


```text
<?xml version='1.0' encoding='utf-8'?>
<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-3" xsi:schemaLocation="http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3/metadata.xsd">
  <identifier identifierType="DOI">10.5281/zenodo.572636</identifier>
  <creators>
    <creator>
      <creatorName>Jason Maassen</creatorName>
      <affiliation>Netherlands eScience Center</affiliation>
    </creator>
    <creator>
      <creatorName>Stefan Verhoeven</creatorName>
      <affiliation>Nederlands eScience Center</affiliation>
    </creator>
    <creator>
      <creatorName>Joris Borgdorff</creatorName>
      <affiliation>@thehyve</affiliation>
    </creator>
    <creator>
      <creatorName>Niels Drost</creatorName>
      <affiliation>Netherlands eScience Center</affiliation>
    </creator>
    <creator>
      <creatorName>Christiaan Meijer</creatorName>
      <affiliation>Netherlands eScience Center</affiliation>
    </creator>
    <creator>
      <creatorName>Jurriaan H. Spaaks</creatorName>
      <affiliation>Netherlands eScience Center</affiliation>
    </creator>
    <creator>
      <creatorName>Rob V. van Nieuwpoort</creatorName>
      <affiliation>Netherlands eScience center</affiliation>
    </creator>
    <creator>
      <creatorName>Piter T. de Boer</creatorName>
    </creator>
    <creator>
      <creatorName>Ben van Werkhoven</creatorName>
      <affiliation>Netherlands eScience Center</affiliation>
    </creator>
  </creators>
  <titles>
    <title>Nlesc/Xenon: Xenon 1.2.2</title>
  </titles>
  <publisher>Zenodo</publisher>
  <publicationYear>2017</publicationYear>
  <dates>
    <date dateType="Issued">2017-05-08</date>
  </dates>
  <resourceType resourceTypeGeneral="Software"/>
  <alternateIdentifiers>
    <alternateIdentifier alternateIdentifierType="url">https://zenodo.org/record/572636</alternateIdentifier>
  </alternateIdentifiers>
  <relatedIdentifiers>
    <relatedIdentifier relatedIdentifierType="URL" relationType="IsSupplementTo">https://github.com/NLeSC/Xenon/tree/1.2.2</relatedIdentifier>
    <relatedIdentifier relatedIdentifierType="DOI" relationType="IsPartOf">10.5281/zenodo.597993</relatedIdentifier>
  </relatedIdentifiers>
  <rightsList>
    <rights rightsURI="info:eu-repo/semantics/openAccess">Open Access</rights>
  </rightsList>
  <descriptions>
    <description descriptionType="Abstract">&lt;p&gt;This is release 1.2.2 of Xenon.&lt;/p&gt;
&lt;p&gt;Bugfixes:&lt;/p&gt;
&lt;ul&gt;
&lt;li&gt;fixed bug in the copy engine that would ignore a copy if source and destination had exactly the same path (even on different machines).&lt;/li&gt;
&lt;li&gt;added timeout overflow detection in Jobs.waitUntilDone and Jobs.waitUntilRunning.&lt;/li&gt;
&lt;/ul&gt;
&lt;p&gt;Other changes:&lt;/p&gt;
&lt;ul&gt;
&lt;li&gt;we have a new logo!&lt;/li&gt;
&lt;li&gt;added SonarQube code for quality analysis and coverage&lt;/li&gt;
&lt;/ul&gt;
&lt;p&gt;What's missing:&lt;/p&gt;
&lt;p&gt;The GridFTP adaptor is not considered stable yet. It is not part of this release.&lt;/p&gt;
&lt;p&gt;There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.&lt;/p&gt;
&lt;p&gt;It should be made easier to inspect at runtime which adaptors are available and what properties they support.&lt;/p&gt;
&lt;p&gt;We can always use more adaptors, e.g, for S3, SWIFT, HDFS, YARN, Azure-Batch, Amazon-Batch etc. These are planned for 1.3 or later.&lt;/p&gt;
&lt;p&gt;We can always use more interfaces, e.g. for clouds. This is planned for 2.0.&lt;/p&gt;</description>
  </descriptions>
</resource>
```

# Dublin Core


```text
<?xml version='1.0' encoding='utf-8'?>
<oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
  <dc:creator>Jason Maassen</dc:creator>
  <dc:creator>Stefan Verhoeven</dc:creator>
  <dc:creator>Joris Borgdorff</dc:creator>
  <dc:creator>Niels Drost</dc:creator>
  <dc:creator>Christiaan Meijer</dc:creator>
  <dc:creator>Jurriaan H. Spaaks</dc:creator>
  <dc:creator>Rob V. van Nieuwpoort</dc:creator>
  <dc:creator>Piter T. de Boer</dc:creator>
  <dc:creator>Ben van Werkhoven</dc:creator>
  <dc:date>2017-05-08</dc:date>
  <dc:description>&lt;p&gt;This is release 1.2.2 of Xenon.&lt;/p&gt;
&lt;p&gt;Bugfixes:&lt;/p&gt;
&lt;ul&gt;
&lt;li&gt;fixed bug in the copy engine that would ignore a copy if source and destination had exactly the same path (even on different machines).&lt;/li&gt;
&lt;li&gt;added timeout overflow detection in Jobs.waitUntilDone and Jobs.waitUntilRunning.&lt;/li&gt;
&lt;/ul&gt;
&lt;p&gt;Other changes:&lt;/p&gt;
&lt;ul&gt;
&lt;li&gt;we have a new logo!&lt;/li&gt;
&lt;li&gt;added SonarQube code for quality analysis and coverage&lt;/li&gt;
&lt;/ul&gt;
&lt;p&gt;What's missing:&lt;/p&gt;
&lt;p&gt;The GridFTP adaptor is not considered stable yet. It is not part of this release.&lt;/p&gt;
&lt;p&gt;There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.&lt;/p&gt;
&lt;p&gt;It should be made easier to inspect at runtime which adaptors are available and what properties they support.&lt;/p&gt;
&lt;p&gt;We can always use more adaptors, e.g, for S3, SWIFT, HDFS, YARN, Azure-Batch, Amazon-Batch etc. These are planned for 1.3 or later.&lt;/p&gt;
&lt;p&gt;We can always use more interfaces, e.g. for clouds. This is planned for 2.0.&lt;/p&gt;</dc:description>
  <dc:identifier>https://zenodo.org/record/572636</dc:identifier>
  <dc:identifier>10.5281/zenodo.572636</dc:identifier>
  <dc:identifier>oai:zenodo.org:572636</dc:identifier>
  <dc:relation>url:https://github.com/NLeSC/Xenon/tree/1.2.2</dc:relation>
  <dc:relation>doi:10.5281/zenodo.597993</dc:relation>
  <dc:rights>info:eu-repo/semantics/openAccess</dc:rights>
  <dc:title>NLeSC/Xenon: Xenon 1.2.2</dc:title>
  <dc:type>info:eu-repo/semantics/other</dc:type>
  <dc:type>software</dc:type>
</oai_dc:dc>

```

# JSON


```text
{
  "conceptdoi": "10.5281/zenodo.597993", 
  "conceptrecid": "597993", 
  "created": "2017-05-08T12:36:27.946922+00:00", 
  "doi": "10.5281/zenodo.572636", 
  "id": 572636, 
  "links": {
    "badge": "https://zenodo.org/badge/doi/10.5281/zenodo.572636.svg", 
    "conceptbadge": "https://zenodo.org/badge/doi/10.5281/zenodo.597993.svg", 
    "conceptdoi": "https://doi.org/10.5281/zenodo.597993", 
    "doi": "https://doi.org/10.5281/zenodo.572636"
  }, 
  "metadata": {
    "access_right": "open", 
    "access_right_category": "success", 
    "creators": [
      {
        "affiliation": "Netherlands eScience Center", 
        "name": "Jason Maassen"
      }, 
      {
        "affiliation": "Nederlands eScience Center", 
        "name": "Stefan Verhoeven"
      }, 
      {
        "affiliation": "@thehyve", 
        "name": "Joris Borgdorff"
      }, 
      {
        "affiliation": "Netherlands eScience Center", 
        "name": "Niels Drost"
      }, 
      {
        "affiliation": "Netherlands eScience Center", 
        "name": "Christiaan Meijer"
      }, 
      {
        "affiliation": "Netherlands eScience Center", 
        "name": "Jurriaan H. Spaaks"
      }, 
      {
        "affiliation": "Netherlands eScience center", 
        "name": "Rob V. van Nieuwpoort"
      }, 
      {
        "name": "Piter T. de Boer"
      }, 
      {
        "affiliation": "Netherlands eScience Center", 
        "name": "Ben van Werkhoven"
      }
    ], 
    "description": "<p>This is release 1.2.2 of Xenon.</p>\n<p>Bugfixes:</p>\n<ul>\n<li>fixed bug in the copy engine that would ignore a copy if source and destination had exactly the same path (even on different machines).</li>\n<li>added timeout overflow detection in Jobs.waitUntilDone and Jobs.waitUntilRunning.</li>\n</ul>\n<p>Other changes:</p>\n<ul>\n<li>we have a new logo!</li>\n<li>added SonarQube code for quality analysis and coverage</li>\n</ul>\n<p>What's missing:</p>\n<p>The GridFTP adaptor is not considered stable yet. It is not part of this release.</p>\n<p>There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.</p>\n<p>It should be made easier to inspect at runtime which adaptors are available and what properties they support.</p>\n<p>We can always use more adaptors, e.g, for S3, SWIFT, HDFS, YARN, Azure-Batch, Amazon-Batch etc. These are planned for 1.3 or later.</p>\n<p>We can always use more interfaces, e.g. for clouds. This is planned for 2.0.</p>", 
    "doi": "10.5281/zenodo.572636", 
    "license": {
      "id": "other-open"
    }, 
    "publication_date": "2017-05-08", 
    "related_identifiers": [
      {
        "identifier": "https://github.com/NLeSC/Xenon/tree/1.2.2", 
        "relation": "isSupplementTo", 
        "scheme": "url"
      }, 
      {
        "identifier": "10.5281/zenodo.597993", 
        "relation": "isPartOf", 
        "scheme": "doi"
      }
    ], 
    "relations": {
      "version": [
        {
          "count": 6, 
          "index": 5, 
          "is_last": true, 
          "last_child": {
            "pid_type": "recid", 
            "pid_value": "572636"
          }, 
          "parent": {
            "pid_type": "recid", 
            "pid_value": "597993"
          }
        }
      ]
    }, 
    "resource_type": {
      "title": "Software", 
      "type": "software"
    }, 
    "title": "NLeSC/Xenon: Xenon 1.2.2"
  }, 
  "owners": [
    19641
  ], 
  "revision": 2, 
  "updated": "2017-05-30T09:49:37.100438+00:00"
}
```

# MARKCXML


```text
<?xml version='1.0' encoding='UTF-8'?>
<record xmlns="http://www.loc.gov/MARC21/slim">
  <leader>00000nmm##2200000uu#4500</leader>
  <datafield tag="540" ind1=" " ind2=" ">
    <subfield code="a">Other (Open)</subfield>
  </datafield>
  <datafield tag="260" ind1=" " ind2=" ">
    <subfield code="c">2017-05-08</subfield>
  </datafield>
  <controlfield tag="005">20170530094937.0</controlfield>
  <datafield tag="773" ind1=" " ind2=" ">
    <subfield code="n">url</subfield>
    <subfield code="i">isSupplementTo</subfield>
    <subfield code="a">https://github.com/NLeSC/Xenon/tree/1.2.2</subfield>
  </datafield>
  <datafield tag="773" ind1=" " ind2=" ">
    <subfield code="n">doi</subfield>
    <subfield code="i">isPartOf</subfield>
    <subfield code="a">10.5281/zenodo.597993</subfield>
  </datafield>
  <controlfield tag="001">572636</controlfield>
  <datafield tag="909" ind1="C" ind2="O">
    <subfield code="p">software</subfield>
    <subfield code="o">oai:zenodo.org:572636</subfield>
  </datafield>
  <datafield tag="520" ind1=" " ind2=" ">
    <subfield code="a">&lt;p&gt;This is release 1.2.2 of Xenon.&lt;/p&gt;
&lt;p&gt;Bugfixes:&lt;/p&gt;
&lt;ul&gt;
&lt;li&gt;fixed bug in the copy engine that would ignore a copy if source and destination had exactly the same path (even on different machines).&lt;/li&gt;
&lt;li&gt;added timeout overflow detection in Jobs.waitUntilDone and Jobs.waitUntilRunning.&lt;/li&gt;
&lt;/ul&gt;
&lt;p&gt;Other changes:&lt;/p&gt;
&lt;ul&gt;
&lt;li&gt;we have a new logo!&lt;/li&gt;
&lt;li&gt;added SonarQube code for quality analysis and coverage&lt;/li&gt;
&lt;/ul&gt;
&lt;p&gt;What's missing:&lt;/p&gt;
&lt;p&gt;The GridFTP adaptor is not considered stable yet. It is not part of this release.&lt;/p&gt;
&lt;p&gt;There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.&lt;/p&gt;
&lt;p&gt;It should be made easier to inspect at runtime which adaptors are available and what properties they support.&lt;/p&gt;
&lt;p&gt;We can always use more adaptors, e.g, for S3, SWIFT, HDFS, YARN, Azure-Batch, Amazon-Batch etc. These are planned for 1.3 or later.&lt;/p&gt;
&lt;p&gt;We can always use more interfaces, e.g. for clouds. This is planned for 2.0.&lt;/p&gt;</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">Nederlands eScience Center</subfield>
    <subfield code="a">Stefan Verhoeven</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">@thehyve</subfield>
    <subfield code="a">Joris Borgdorff</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">Netherlands eScience Center</subfield>
    <subfield code="a">Niels Drost</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">Netherlands eScience Center</subfield>
    <subfield code="a">Christiaan Meijer</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">Netherlands eScience Center</subfield>
    <subfield code="a">Jurriaan H. Spaaks</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">Netherlands eScience center</subfield>
    <subfield code="a">Rob V. van Nieuwpoort</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="a">Piter T. de Boer</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="u">Netherlands eScience Center</subfield>
    <subfield code="a">Ben van Werkhoven</subfield>
  </datafield>
  <datafield tag="856" ind1="4" ind2=" ">
    <subfield code="s">26991773</subfield>
    <subfield code="z">md5:b3e0ffef54f2bf148c5d57ce237d724f</subfield>
    <subfield code="u">https://zenodo.org/record/572636/files/NLeSC/Xenon-1.2.2.zip</subfield>
  </datafield>
  <datafield tag="542" ind1=" " ind2=" ">
    <subfield code="l">open</subfield>
  </datafield>
  <datafield tag="980" ind1=" " ind2=" ">
    <subfield code="a">software</subfield>
  </datafield>
  <datafield tag="100" ind1=" " ind2=" ">
    <subfield code="u">Netherlands eScience Center</subfield>
    <subfield code="a">Jason Maassen</subfield>
  </datafield>
  <datafield tag="024" ind1=" " ind2=" ">
    <subfield code="a">10.5281/zenodo.572636</subfield>
    <subfield code="2">doi</subfield>
  </datafield>
  <datafield tag="245" ind1=" " ind2=" ">
    <subfield code="a">NLeSC/Xenon: Xenon 1.2.2</subfield>
  </datafield>
  <datafield tag="650" ind1="1" ind2="7">
    <subfield code="a">cc-by</subfield>
    <subfield code="2">opendefinition.org</subfield>
  </datafield>
</record>
```

