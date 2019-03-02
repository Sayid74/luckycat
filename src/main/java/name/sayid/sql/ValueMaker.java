package name.sayid.sql;

/**
 * Value maker means the maker can make something to a correct java object.
 * @param <T> The type of maker should made.
 */
public interface ValueMaker<T> {
    /**
     * The maker effect method.
     * @return The made object.
     * @throws ValueIllegal
     */
    T makeValue() throws ValueIllegal;
}
