package main;

import UtilitiesFx.filesTools.PathTools;

public class Config {

	public String project_path = PathTools.asFolder("mnt") + PathTools.asFolder("c")  + PathTools.asFolder("Users") + PathTools.asFolder("byari-m")
			+ PathTools.asFolder("Desktop") + PathTools.asFolder("CRAFTY-Cobra-Code-compareson")
			+ PathTools.asFolder("data_for_UI");
	public String scenario = "RCP2_6-SSP4";
	public boolean regionalization = false;
	// CRAFTY Mechanisms
	public boolean initial_demand_supply_equilibrium = true;
	public boolean remove_negative_marginal_utility = false;
	public boolean use_abandonment_threshold = true;
	public boolean mutate_on_competition_win = false;
	public double mutation_interval = 0.01;
	public double MostCompetitorAFTProbability = 0.8;
	public boolean averaged_residual_demand_per_cell = false;
	// Neighboring Effects
	public boolean use_neighbor_priority = true;
	public double neighbor_priority_probability = 0.95;
	public int neighbor_radius = 2;
	// Competitiveness Process
	public double participating_cells_percentage = 0.03;
	public int marginal_utility_calculations_per_tick = 10;
	public double land_abandonment_percentage = 0.05;
	// Output Configurati
	public String output_folder_name = "linux";
	public boolean generate_csv_files = true;
	public int csv_output_frequency = 50;
	public boolean track_changes = false;

}