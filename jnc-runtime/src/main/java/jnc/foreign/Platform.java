package jnc.foreign;

import javax.annotation.Nonnull;
import jnc.foreign.spi.ForeignProvider;

@SuppressWarnings("PublicInnerClass")
public interface Platform {

    @Nonnull
    static Platform getNativePlatform() {
        return ForeignProvider.getDefault().getPlatform();
    }

    @Nonnull
    String getLibcName();

    @Nonnull
    OS getOS();

    @Nonnull
    Arch getArch();

    enum Arch {

        UNKNOWN(0),
        I386(4),
        X86_64(8);

        private final int size;

        Arch(int size) {
            this.size = size;
        }

        public int sizeOfPointer() {
            return size;
        }

    }

    enum OS {

        UNKNOWN(OS.FLAG_NONE),
        WINDOWS(OS.FLAG_WINDOWS),
        LINUX(OS.FLAG_UNIX | OS.FLAG_ELF),
        FREEBSD(OS.FLAG_UNIX | OS.FLAG_BSD | OS.FLAG_ELF),
        OPENBSD(OS.FLAG_UNIX | OS.FLAG_BSD | OS.FLAG_ELF),
        DARWIN(OS.FLAG_UNIX | OS.FLAG_BSD), /*
        AIX(OS.FLAG_UNIX),
        HPUX(OS.FLAG_UNIX | OS.FLAG_ELF),
        OPENVMS(OS.FLAG_UNIX | OS.FLAG_ELF)
         */;

        private static final int FLAG_NONE = 0;
        private static final int FLAG_WINDOWS = 1; // _WIN32
        private static final int FLAG_UNIX = 2; // __unix__
        private static final int FLAG_ELF = 4; // __ELF__
        private static final int FLAG_BSD = 8;  // BSD

        private final int flag;

        OS(int flag) {
            this.flag = flag;
        }

        public boolean isWindows() {
            return (flag & FLAG_WINDOWS) != 0;
        }

        public boolean isBSD() {
            return (flag & FLAG_BSD) != 0;
        }

        public boolean isUnix() {
            return (flag & FLAG_UNIX) != 0;
        }

        public boolean isELF() {
            return (flag & FLAG_ELF) != 0;
        }

    }

}
