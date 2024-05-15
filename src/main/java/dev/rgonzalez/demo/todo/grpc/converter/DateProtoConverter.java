package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.proto.todo.common.messages.Date;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class DateProtoConverter implements
        ProtoConverter<Date, LocalDate>,
        ModelConverter<LocalDate, Date> {

    @Override
    public Optional<Date> toProto(LocalDate model) {
        return Optional.ofNullable(model)
                .map(localDate -> Date.newBuilder()
                        .setYear(localDate.getYear())
                        .setMonth(localDate.getMonthValue())
                        .setDay(localDate.getDayOfMonth())
                        .build());
    }

    @Override
    public Optional<LocalDate> toModel(Date proto) {
        return Optional.ofNullable(proto)
                .map(date -> LocalDate.of(date.getYear(), date.getMonth(), date.getDay()));
    }

}
