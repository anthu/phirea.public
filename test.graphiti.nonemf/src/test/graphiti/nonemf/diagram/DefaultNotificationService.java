package test.graphiti.nonemf.diagram;

import java.util.ArrayList;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.notification.INotificationService;

/**
 * @author Nikolai Raitsev
 *
 */
public class DefaultNotificationService implements INotificationService {

	private IDiagramTypeProvider diagramTypeProvider;

	/**
	 * Creates a new {@link DefaultNotificationService}.
	 * 
	 * @param diagramTypeProvider
	 *            the diagram type provider
	 */
	public DefaultNotificationService(IDiagramTypeProvider diagramTypeProvider) {
		this.diagramTypeProvider = diagramTypeProvider;
	}

	/**
	 * Gets the diagram type provider.
	 * 
	 * @return the diagram type provider
	 */
	protected IDiagramTypeProvider getDiagramTypeProvider() {
		return this.diagramTypeProvider;
	}

	/**
	 * Update dirty pictogram elements.
	 * 
	 * @param dirtyPes
	 *            the dirty pes
	 */
	public void updatePictogramElements(PictogramElement[] dirtyPes) {
		final IDiagramTypeProvider dtp = getDiagramTypeProvider();
		final IFeatureProvider fp = dtp.getFeatureProvider();
		for (PictogramElement pe : dirtyPes) {
			final UpdateContext updateContext = new UpdateContext(pe);
			// fp.updateIfPossible(updateContext);
			fp.updateIfPossibleAndNeeded(updateContext);
		}
	}

	/**
	 * Calculate linked pictogram elements.
	 * 
	 * @param changedAndRelatedBOsList
	 *            the changed and related BOs list
	 * @return the pictogram element[]
	 */
	protected PictogramElement[] calculateLinkedPictogramElements(ArrayList<Object> changedAndRelatedBOsList) {
		ArrayList<PictogramElement> retList = new ArrayList<PictogramElement>();
		final IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
		for (Object crbo : changedAndRelatedBOsList) {
			final PictogramElement[] allPictogramElementsForBusinessObject = featureProvider.getAllPictogramElementsForBusinessObject(crbo);
			for (PictogramElement pe : allPictogramElementsForBusinessObject) {
				retList.add(pe);
			}
		}
		return retList.toArray(new PictogramElement[0]);
	}

	/**
	 * Calculate dirty pictogram elements.
	 * 
	 * @param changedBOs
	 *            the changed business objects
	 * @return the pictogram element[]
	 */
	public PictogramElement[] calculateRelatedPictogramElements(Object[] changedBOs) {
		ArrayList<Object> changedAndRelatedBOsList = new ArrayList<Object>();
		for (Object cbo : changedBOs) {
			changedAndRelatedBOsList.add(cbo);
		}
		Object[] relatedBOs = getDiagramTypeProvider().getRelatedBusinessObjects(changedBOs);
		for (Object rbo : relatedBOs) {
			changedAndRelatedBOsList.add(rbo);
		}

		return calculateLinkedPictogramElements(changedAndRelatedBOsList);
	}

}
