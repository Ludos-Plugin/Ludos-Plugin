package fr.ludos.core.item;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a collection of material types used as a category filter.
 */
public interface Category extends Collection<Material> {
	public boolean contains(Material type);
	public default boolean contains(ItemStack item) {
		if (item == null) return false;
		return contains(item.getType());
	}
	@Override
	default boolean contains(Object o) {
		if (! (o instanceof Material material)) return false;
		return contains(material);
	}

	public static Category of(final Material... types) {
		return new SimpleCategory(types);
	}

	public static Category union(final Category... categories) {
		return new CompoundCategory(categories);
	}

	/**
	 * Represents a category backed by a set of material types.
	 */
	public static class SimpleCategory implements Category {
		public final Set<Material> materials;

		public SimpleCategory(final Set<Material> types) {
			this.materials = types;
		}
		public SimpleCategory(final Material... types) {
			this(Set.of(types));
		}

		@Override
		public boolean contains(Material type) {
			return materials.contains(type);
		}

		@Override
		public Iterator<Material> iterator() {
			return materials.iterator();
		}

		@Override
		public int size() {
			return materials.size();
		}
		@Override
		public boolean isEmpty() {
			return materials.isEmpty();
		}
		@Override
		public Object[] toArray() {
			return materials.toArray();
		}
		@Override
		public <T> T[] toArray(T[] a) {
			return materials.toArray(a);
		}
		@Override
		public boolean add(Material e) {
			return materials.add(e);
		}
		@Override
		public boolean remove(Object o) {
			return materials.remove(o);
		}
		@Override
		public boolean containsAll(Collection<?> c) {
			return materials.containsAll(c);
		}
		@Override
		public boolean addAll(Collection<? extends Material> c) {
			return materials.addAll(c);
		}
		@Override
		public boolean removeAll(Collection<?> c) {
			return materials.removeAll(c);
		}
		@Override
		public boolean retainAll(Collection<?> c) {
			return materials.retainAll(c);
		}
		@Override
		public void clear() {
			materials.clear();
		}
	}

	/**
	 * Represents a category that aggregates multiple child categories.
	 * Membership is computed as the union of all contained categories.
	 */
	public static class CompoundCategory implements Category {
		public final List<Category> categories;

		public CompoundCategory(final List<Category> categories) {
			this.categories = categories;
		}
		public CompoundCategory(final Category... categories) {
			this(List.of(categories));
		}

		@Override
		public boolean contains(Material type) {
			for (Category category : categories) {
				if (category.contains(type)) return true;
			}
			return false;
		}
		@Override
		public Stream<Material> stream() {
			return categories.stream()
				.flatMap(Category::stream);
		}
		@Override
		public Iterator<Material> iterator() {
			return stream().iterator();
		}
		@Override
		public int size() {
			return categories.stream()
				.reduce(0, (sum, c) -> sum + c.size(), Integer::sum);
		}
		@Override
		public boolean isEmpty() {
			return categories.stream()
				.allMatch(inner -> inner == null || inner.isEmpty());
		}
		@Override
		public Object[] toArray() {
			return stream().toArray();
		}
		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			return stream().toArray(size -> {
				if (size <= a.length) return a;
				return (T[]) Array.newInstance(a.getClass().getComponentType(), size);
			});
		}
		@Override
		public boolean containsAll(Collection<?> c) {
			return c.stream()
				.allMatch(this::contains);
		}
		@Override
		public boolean add(Material e) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean addAll(Collection<? extends Material> c) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
	}
}
