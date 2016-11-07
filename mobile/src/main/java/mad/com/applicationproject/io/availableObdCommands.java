package mad.com.applicationproject.io;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.control.DtcNumberCommand;
import com.github.pires.obd.commands.control.EquivalentRatioCommand;
import com.github.pires.obd.commands.control.IgnitionMonitorCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.PendingTroubleCodesCommand;
import com.github.pires.obd.commands.control.PermanentTroubleCodesCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.engine.AbsoluteLoadCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.FuelTrimCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_01_20;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_41_60;
import com.github.pires.obd.commands.protocol.CloseCommand;
import com.github.pires.obd.commands.protocol.DescribeProtocolCommand;
import com.github.pires.obd.commands.protocol.DescribeProtocolNumberCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.HeadersOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.ObdWarmstartCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.commands.protocol.SpacesOffCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.FuelTrim;

import java.util.ArrayList;

/**
 * TODO put description
 */
public final class availableObdCommands {

    public static ArrayList<ObdCommand> getCommands() {
        ArrayList<ObdCommand> obdCommands = new ArrayList<>();

        // Control
        obdCommands.add(new EquivalentRatioCommand());
        obdCommands.add(new ModuleVoltageCommand());
        obdCommands.add(new TimingAdvanceCommand());
        obdCommands.add(new VinCommand());

        // Engine
        obdCommands.add(new AbsoluteLoadCommand()); // raw value
        obdCommands.add(new LoadCommand());         // %
        obdCommands.add(new MassAirFlowCommand());
        obdCommands.add(new OilTempCommand());
        obdCommands.add(new RPMCommand());
        obdCommands.add(new RuntimeCommand());
        obdCommands.add(new ThrottlePositionCommand());

        // Fuel
        obdCommands.add(new AirFuelRatioCommand());
        obdCommands.add(new ConsumptionRateCommand());
        obdCommands.add(new FindFuelTypeCommand());
        obdCommands.add(new FuelLevelCommand());
        obdCommands.add(new FuelTrimCommand(FuelTrim.SHORT_TERM_BANK_1));
        obdCommands.add(new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_1));
        obdCommands.add(new FuelTrimCommand(FuelTrim.SHORT_TERM_BANK_2));
        obdCommands.add(new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_2));
        obdCommands.add(new WidebandAirFuelRatioCommand());
        obdCommands.add(new OilTempCommand());

        // Pressure
        obdCommands.add(new BarometricPressureCommand());
        obdCommands.add(new FuelPressureCommand());
        obdCommands.add(new FuelRailPressureCommand());
        obdCommands.add(new IntakeManifoldPressureCommand());

        // Temperature
        obdCommands.add(new AirIntakeTemperatureCommand());
        obdCommands.add(new AmbientAirTemperatureCommand());
        obdCommands.add(new EngineCoolantTemperatureCommand());

        // Misc
        obdCommands.add(new SpeedCommand());

        return obdCommands;
    }

    public static ArrayList<ObdCommand> getTroubleCommands() {
        ArrayList<ObdCommand> obdTroubleCommands = new ArrayList<>();

        // Trouble codes
        obdTroubleCommands.add(new DistanceMILOnCommand());  // Distance traveled with MIL on
        obdTroubleCommands.add(new DistanceSinceCCCommand());// Distance traveled since codes cleared
        obdTroubleCommands.add(new DtcNumberCommand());      // Monitor status since DTCs cleared
        obdTroubleCommands.add(new IgnitionMonitorCommand());
        obdTroubleCommands.add(new PendingTroubleCodesCommand());
        obdTroubleCommands.add(new PermanentTroubleCodesCommand());
        obdTroubleCommands.add(new TroubleCodesCommand());

        // DTC = diagnostic trouble codes
        // MIL = malfunction indicator lamp (AKA check engine light)

        return obdTroubleCommands;
    }

    public static ArrayList<ObdCommand> getProtocolCommands() {
        ArrayList<ObdCommand> obdProtocolCommands = new ArrayList<>();


        // protocol
        obdProtocolCommands.add(new AvailablePidsCommand_01_20());
        obdProtocolCommands.add(new AvailablePidsCommand_21_40());
        obdProtocolCommands.add(new AvailablePidsCommand_41_60());
        obdProtocolCommands.add(new CloseCommand());
        obdProtocolCommands.add(new DescribeProtocolCommand());
        obdProtocolCommands.add(new DescribeProtocolNumberCommand());
        obdProtocolCommands.add(new EchoOffCommand());
        obdProtocolCommands.add(new HeadersOffCommand());
        obdProtocolCommands.add(new LineFeedOffCommand());
//        obdProtocolCommands.add(new ObdRawCommand()); // allows unspecified commands
        obdProtocolCommands.add(new ObdResetCommand());
        obdProtocolCommands.add(new ObdWarmstartCommand());
        obdProtocolCommands.add(new ResetTroubleCodesCommand());
//        obdProtocolCommands.add(new SelectProtocolCommand()); // gets protocol command from ObdProtocol object
        obdProtocolCommands.add(new SpacesOffCommand());
        obdProtocolCommands.add(new TimeoutCommand(0)); // sets time to wait for a response

        return obdProtocolCommands;
    }
}
