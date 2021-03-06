/* CVS $Id: $ */
package de.hpi.patchr.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from ./vocabulary/patchr.ttl
 *
 * @author Auto-generated by schemagen on 30 Jan 2015 16:46
 */
public class PatchrOntology {
    /**
     * <p>The RDF model that holds the vocabulary terms</p>
     */
    private static Model m_model = ModelFactory.createDefaultModel();

    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://purl.org/hpi/patchr#";

    /**
     * <p>The namespace of the vocabulary as a string</p>
     *
     * @see #NS
     */
    public static String getURI() {
        return NS;
    }

    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = m_model.createResource(NS);

    /**
     * <p>The ontology's owl:versionInfo as a string</p>
     */
    public static final String VERSION_INFO = "Revision: 0.6";

    /**
     * <p>This property is deprecated and should not be used any more, rather use a
     * positive confidence value for advocating agents. Links agents who support,
     * i.e. vote for, a :Patch. Each time a user creates a patch request, appropriate
     * provenance information is generated.</p>
     */
    public static final Property advocate = m_model.createProperty("http://purl.org/hpi/patchr#advocate");

    /**
     * <p>Refers to a void:Dataset to allow convenient selection of patches per dataset.</p>
     */
    public static final Property appliesTo = m_model.createProperty("http://purl.org/hpi/patchr#appliesTo");

    /**
     * <p>This property is deprecated and should not be used any more, use dcterms:description
     * instead. A comment on the patch. This is likely just an informal message a
     * user wants to share when submitting a Patch.</p>
     */
    public static final Property comment = m_model.createProperty("http://purl.org/hpi/patchr#comment");

    /**
     * <p>A confidence assigned by the creator of the patch, e.g. in case heuristic
     * methods identified an inconsistency. This confidence must be expressed in
     * the range of [-1,1], whereas a high value means higher confidence and a value
     * of 1 signifies absolute certainty. Negative values indicate criticism towards
     * this patch.</p>
     */
    public static final Property confidence = m_model.createProperty("http://purl.org/hpi/patchr#confidence");

    /**
     * <p>This property is deprecated and should not be used any more, rather use a
     * negative confidence value for advocating agents. Links agents who disagree,
     * i.e. vote against, a :Patch.</p>
     */
    public static final Property criticiser = m_model.createProperty("http://purl.org/hpi/patchr#criticiser");

    /**
     * <p>Relates a Patch to a PatchGroup. A Patch may be part of multiple :PatchGroups.</p>
     */
    public static final Property memberOf = m_model.createProperty("http://purl.org/hpi/patchr#memberOf");

    /**
     * <p>Refers to a classification of the patch. There might be patch taxonomies from
     * different applications that define the reason for a :Patch on their own. A
     * :Patch may have multiple types.</p>
     */
    public static final Property patchType = m_model.createProperty("http://purl.org/hpi/patchr#patchType");

    /**
     * <p>Links a resource to the PatchR instance responsible for collecting patch requests.</p>
     */
    public static final Property patchrService = m_model.createProperty("http://purl.org/hpi/patchr#patchrService");

    /**
     * <p>This property is deprecated and should not be used any more, use prov:wasGeneratedBy
     * instead. Refers to the provenance context where this patch was created.</p>
     */
    public static final Property provenance = m_model.createProperty("http://purl.org/hpi/patchr#provenance");

    /**
     * <p>The status of the patch, might be one of Open, Resolved, Postponed, or Rejected.</p>
     */
    public static final Property status = m_model.createProperty("http://purl.org/hpi/patchr#status");

    /**
     * <p>Refers to a guo:UpdateInstruction. There must be exactly one guo:UpdateInstruction
     * per :Patch. The guo:UpdateInstruction either is a deletion, insertion, or
     * replacement of triples for one particular subject resource.</p>
     */
    public static final Property update = m_model.createProperty("http://purl.org/hpi/patchr#update");

    /**
     * <p>A conjunctive patch group is collection (list) of patches where all member
     * patches need to be applied to the dataset to resolve the problem.</p>
     */
    public static final Resource ConjunctivePatchGroup = m_model.createResource("http://purl.org/hpi/patchr#ConjunctivePatchGroup");

    /**
     * <p>A disjunctive patch group is collection (alternative list) of patches where
     * one member patch need to be applied to resolve the problem.</p>
     */
    public static final Resource DisjunctivePatchGroup = m_model.createResource("http://purl.org/hpi/patchr#DisjunctivePatchGroup");

    /**
     * <p>A patch is a request to add, change or delete certain triples (or subgraphs)
     * within a dataset. A patch refers to an guo:UpdateInstruction. For convenient
     * retrieval a patch should be described with provenance data, a patch type and
     * a dataset it applies to.</p>
     */
    public static final Resource Patch = m_model.createResource("http://purl.org/hpi/patchr#Patch");

    /**
     * <p>A patch group is a collection of patches that apply to a common problem.</p>
     */
    public static final Resource PatchGroup = m_model.createResource("http://purl.org/hpi/patchr#PatchGroup");

    /**
     * <p>The status of the patch, might be one of Open, Resolved, Postponed, or Rejected.</p>
     */
    public static final Resource PatchStatus = m_model.createResource("http://purl.org/hpi/patchr#PatchStatus");

    public static final Resource PatchType = m_model.createResource("http://purl.org/hpi/patchr#PatchType");

    public static final Resource Obsolete = m_model.createResource("http://purl.org/hpi/patchr#Obsolete");

    /**
     * <p>The patch has been raised.</p>
     */
    public static final Resource Open = m_model.createResource("http://purl.org/hpi/patchr#Open");

    /**
     * <p>The patch has been postponed.</p>
     */
    public static final Resource Postponed = m_model.createResource("http://purl.org/hpi/patchr#Postponed");

    /**
     * <p>The patch has been rejected, i.e. this patch will not be applied. It can be
     * more or less ignored but is kept for archival reasons, new requests for that
     * patch will be rejected.</p>
     */
    public static final Resource Rejected = m_model.createResource("http://purl.org/hpi/patchr#Rejected");

    /**
     * <p>The patch has been resolved, i.e. this patch has been applied. It can be more
     * or less ignored but is kept for archival reasons, new requests for that patch
     * will be answered as resolved.</p>
     */
    public static final Resource Resolved = m_model.createResource("http://purl.org/hpi/patchr#Resolved");

}
