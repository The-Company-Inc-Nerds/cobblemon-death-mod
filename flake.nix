{
  description = "A Nuzlocke-style mod that damages the player when their Pokémon faint in battle, removes pokemon on faint or run.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

    zed-editor = {
      url = "github:CalamooseLabs/antlers/flakes.zed-editor?dir=flakes/zed-editor";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = {nixpkgs, ...} @ inputs: let
    system = "x86_64-linux";
    pkgs = import nixpkgs {system = system;};
  in {
    devShells.${system}.default = import ./shell.nix {
      inherit pkgs;
      inherit inputs;
    };
  };
}
