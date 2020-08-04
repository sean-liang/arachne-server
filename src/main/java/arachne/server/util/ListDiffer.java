package arachne.server.util;

import java.util.List;
import java.util.function.Consumer;

/**
 * Diff two list and apply callbacks for removed and added items.
 */
public class ListDiffer {

    /**
     * Diff two list and apply callbacks for removed and added items
     *
     * @param origin original list
     * @param target target list
     * @param onRemoved removed item callback
     * @param onAdded added item callback
     * @param <T>
     */
    public static <T> void diff(final List<T> origin, final List<T> target, final Consumer<T> onRemoved, final Consumer<T> onAdded) {
        if (null != origin) {
            for (final T o : origin) {
                if (null == target || !target.contains(o)) {
                    onRemoved.accept(o);
                }
            }
        }

        if (null != target) {
            for (final T t : origin) {
                if (null == origin || !origin.contains(t)) {
                    onAdded.accept(t);
                }
            }
        }
    }

}
