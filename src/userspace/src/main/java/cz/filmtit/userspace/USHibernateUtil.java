package cz.filmtit.userspace;

import cz.filmtit.core.CoreHibernateUtil;

public class USHibernateUtil extends CoreHibernateUtil {
    protected static USHibernateUtil instance = null;

    protected USHibernateUtil() {
        super();
    }

    public static USHibernateUtil getInstance() {
        if (instance == null) {
            instance = new USHibernateUtil();
        }
        return instance;
    }

    // use the user space path instead of the original core one
    @Override
    public java.net.URL getConfigurationFile() {
        return USHibernateUtil.class.getResource("/cz/filmtit/userspace/userspace.cfg.xml");
    }

}
