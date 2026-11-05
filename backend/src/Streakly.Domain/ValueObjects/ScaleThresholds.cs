using Streakly.Domain.Exceptions;
namespace Streakly.Domain.ValueObjects;

public class ScaleThresholds
{
    public decimal Minimum { get; }
    public decimal Good { get; }
    public decimal Excellent { get; }
    public decimal Maximum { get; }
    public string Unit { get; } = string.Empty;

    protected ScaleThresholds() { }
    public ScaleThresholds(decimal minimum, decimal good, decimal excellent, decimal maximum, string unit)
    {
        if (minimum <= 0 || good <= 0 || excellent <= 0 || maximum <= 0) throw new DomainException("Los valores deben ser mayores a 0");
        if (minimum >= good || good >= excellent || excellent >= maximum) throw new DomainException("Los valores deben estar en orden ascendente");
        if (string.IsNullOrWhiteSpace(unit)) throw new DomainException("La unidad no puede estar vacia");

        Minimum = minimum;
        Good = good;
        Excellent = excellent;
        Maximum = maximum;
        Unit = unit;
    }
}