package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.*;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

public class ExceptionUtils {

    public static <T> T exceptionHandlers(Supplier<T> supplier, String dataExistsErrorMessage) {
        try {
            return supplier.get();
        } catch (DataIntegrityViolationException e) {
            throw new DataExistsException(dataExistsErrorMessage);
        } catch (DataException | BadRequestException e) {
            throw new BadRequestException(e.getLocalizedMessage());
        } catch (EmailNotValidException e) {
            throw new EmailNotValidException(e.getLocalizedMessage());
        } catch (PasswordException e) {
            throw new PasswordException(e.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

}
