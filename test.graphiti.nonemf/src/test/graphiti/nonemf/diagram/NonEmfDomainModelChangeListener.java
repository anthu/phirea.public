package test.graphiti.nonemf.diagram;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.swt.widgets.Display;

/**
 * @author Nikolai Raitsev
 *
 */
public class NonEmfDomainModelChangeListener implements ResourceSetListener {

	private IDiagramEditor diagramEditor;

	public NonEmfDomainModelChangeListener(IDiagramEditor diagramEditor) {
		setDiagramEditor(diagramEditor);
	}

	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {

		//		// if there is no diagramLink, we have also no pictogramLinks -> no
		//		// references to bo's -> don't handle change events
		//		if (getDiagramTypeProvider() instanceof AbstractDiagramTypeProvider) {
		//			DiagramLink cachedDiagramLink = ((AbstractDiagramTypeProvider) getDiagramTypeProvider()).getCachedDiagramLink();
		//			if (cachedDiagramLink == null) {
		//				return;
		//			}
		//		}
		// if we have no pictogramLinks -> no
		// references to bo's -> don't handle change events
		Diagram diagram = getDiagramTypeProvider().getDiagram();
		if (diagram != null) {
			if (diagram.getPictogramLinks().size() == 0) {
				return;
			}
		}

		// Compute changed BOs.
		final Set<EObject> changedBOs = new HashSet<EObject>();
		List<Notification> notifications = event.getNotifications();
		for (Notification notification : notifications) {
			Object notifier = notification.getNotifier();
			if (!(notifier instanceof EObject)) {
				continue;
			}
			changedBOs.add((EObject) notifier);
		}

		final PictogramElement[] dirtyPes = getDiagramTypeProvider().getNotificationService().calculateRelatedPictogramElements(
				changedBOs.toArray());

		// Do nothing if no BO linked to the diagram changed.
		if (dirtyPes.length == 0) {
			return;
		}

		// Do an asynchronous update in the UI thread.
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (getDiagramTypeProvider().isAutoUpdateAtRuntime() && getDiagramTypeProvider().getDiagramEditor().isDirty()) {
					// The notification service takes care of not only the
					// linked BOs but also asks the diagram provider about
					// related BOs.
					getDiagramTypeProvider().getNotificationService().updatePictogramElements(dirtyPes);
				} else {
					getDiagramTypeProvider().getDiagramEditor().refresh();
				}
			}

		});

	}

	@Override
	public Command transactionAboutToCommit(ResourceSetChangeEvent event) throws RollbackException {
		return null;
	}

	private IDiagramTypeProvider getDiagramTypeProvider() {
		return getDiagramEditor().getDiagramTypeProvider();
	}

	private IDiagramEditor getDiagramEditor() {
		return diagramEditor;
	}

	private void setDiagramEditor(IDiagramEditor diagramEditor) {
		this.diagramEditor = diagramEditor;
	}

	@Override
	public NotificationFilter getFilter() {
		return NotificationFilter.NOT_TOUCH;
	}

	@Override
	public boolean isAggregatePrecommitListener() {
		return false;
	}

	@Override
	public boolean isPostcommitOnly() {
		return true;
	}

	@Override
	public boolean isPrecommitOnly() {
		return false;
	}
}