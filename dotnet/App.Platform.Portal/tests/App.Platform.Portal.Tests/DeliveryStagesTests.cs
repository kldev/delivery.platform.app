using App.Platform.Portal.Platform;

namespace App.Platform.Portal.Tests;

public class DeliveryStagesTests
{
    [Theory]
    [InlineData("CREATED", DeliveryStage.Created)]
    [InlineData("PICKED_UP", DeliveryStage.PickedUp)]
    [InlineData("IN_TRANSIT", DeliveryStage.InTransit)]
    [InlineData("CANCELLED", DeliveryStage.Cancelled)]
    [InlineData("SOMETHING_NEW", DeliveryStage.Unknown)]
    [InlineData(null, DeliveryStage.Unknown)]
    public void Status_names_of_the_api_map_to_stages(string? status, DeliveryStage expected) =>
        DeliveryStages.Parse(status).ShouldBe(expected);

    [Fact]
    public void Every_stage_on_the_route_round_trips_through_its_api_name()
    {
        foreach (var stage in DeliveryStages.Route.Append(DeliveryStage.Cancelled))
            DeliveryStages.Parse(DeliveryStages.ApiName(stage)).ShouldBe(stage);
    }

    [Theory]
    [InlineData(DeliveryStage.Created, DeliveryAction.Confirm)]
    [InlineData(DeliveryStage.Assigned, DeliveryAction.Pickup)]
    [InlineData(DeliveryStage.PickedUp, DeliveryAction.Start)]
    [InlineData(DeliveryStage.InTransit, DeliveryAction.Complete)]
    public void The_next_action_moves_the_delivery_one_stop_on(
        DeliveryStage stage,
        DeliveryAction action
    ) => DeliveryStages.NextAction(stage)!.Action.ShouldBe(action);

    [Theory]
    [InlineData(DeliveryStage.Confirmed)]
    [InlineData(DeliveryStage.Delivered)]
    [InlineData(DeliveryStage.Cancelled)]
    public void A_confirmed_delivery_moves_on_by_its_payment_and_the_last_stops_have_no_next_action(
        DeliveryStage stage
    ) => DeliveryStages.NextAction(stage).ShouldBeNull();

    [Theory]
    [InlineData(DeliveryStage.Created, true)]
    [InlineData(DeliveryStage.Confirmed, false)]
    [InlineData(DeliveryStage.InTransit, true)]
    [InlineData(DeliveryStage.Delivered, false)]
    [InlineData(DeliveryStage.Cancelled, false)]
    public void Cancelling_is_offered_where_the_delivery_service_allows_it(
        DeliveryStage stage,
        bool offered
    ) => DeliveryStages.CanCancel(stage).ShouldBe(offered);

    [Fact]
    public void The_vehicle_is_drawn_from_the_first_to_the_last_stop()
    {
        DeliveryStages.Progress(DeliveryStage.Created).ShouldBe(0);
        DeliveryStages.Progress(DeliveryStage.PickedUp).ShouldBe(0.6, 0.0001);
        DeliveryStages.Progress(DeliveryStage.Delivered).ShouldBe(1);
    }
}
