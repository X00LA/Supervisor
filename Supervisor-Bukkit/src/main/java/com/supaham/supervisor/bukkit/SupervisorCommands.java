package com.supaham.supervisor.bukkit;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.supaham.commons.utils.StringUtils;
import com.supaham.supervisor.report.OutputFormat;
import com.supaham.supervisor.report.Report.ReportResult;
import com.supaham.supervisor.report.ReportSpecifications.ReportSpecsBuilder;
import com.supaham.supervisor.service.ReportServiceManager;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SupervisorCommands {

    @Command(aliases = {"svreport", "sv"}, desc = "Generates a Supervisor report.")
    public void svreport(CommandSender sender,
                         @Switch('v') boolean version,
                         @Switch('t') String title,
                         @Switch('f') String format,
                         @Switch('e') String excludesString,
                         @Switch('i') String includesString) throws CommandException {
        SupervisorPlugin plugin = SupervisorPlugin.get();

        if (version) {
            sender.sendMessage("Supervisor v" + plugin.getDescription().getVersion());
            return;
        }
        excludesString = StringUtils.stripToNull(excludesString);
        includesString = StringUtils.stripToNull(includesString);

        List<String> excludes = excludesString == null ? Collections.<String>emptyList() : Arrays.asList(excludesString.split("\\s+"));
        List<String> includes = includesString == null ? Collections.<String>emptyList() : Arrays.asList(includesString.split("\\s+"));

        ReportSpecsBuilder builder = plugin.createDefaultBuilder().excludes(excludes).includes(includes);
        if (title != null) {
            builder.title(title);
        }
        if (format != null) {
            OutputFormat outputFormat = OutputFormat.getByName(format);
            if (outputFormat == null) {
                throw new CommandException("'" + format + "' is not a valid format.");
            }
            builder.format(outputFormat);
        }

        ReportResult result = SupervisorPlugin.createReport(builder.build()).call();
        ReportServiceManager.createService(result, "gist").publish(result, new CommandSenderMessageRecipient(sender));
    }
}