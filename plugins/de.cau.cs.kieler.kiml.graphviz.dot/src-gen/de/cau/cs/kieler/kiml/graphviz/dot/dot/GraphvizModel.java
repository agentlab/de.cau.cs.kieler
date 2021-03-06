/**
 */
package de.cau.cs.kieler.kiml.graphviz.dot.dot;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Graphviz Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.cau.cs.kieler.kiml.graphviz.dot.dot.GraphvizModel#getGraphs <em>Graphs</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.cau.cs.kieler.kiml.graphviz.dot.dot.DotPackage#getGraphvizModel()
 * @model
 * @generated
 */
public interface GraphvizModel extends EObject
{
  /**
   * Returns the value of the '<em><b>Graphs</b></em>' containment reference list.
   * The list contents are of type {@link de.cau.cs.kieler.kiml.graphviz.dot.dot.Graph}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Graphs</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Graphs</em>' containment reference list.
   * @see de.cau.cs.kieler.kiml.graphviz.dot.dot.DotPackage#getGraphvizModel_Graphs()
   * @model containment="true"
   * @generated
   */
  EList<Graph> getGraphs();

} // GraphvizModel
